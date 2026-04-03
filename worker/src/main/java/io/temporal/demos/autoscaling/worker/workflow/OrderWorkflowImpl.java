package io.temporal.demos.autoscaling.worker.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.demos.autoscaling.worker.activity.*;
import io.temporal.demos.autoscaling.worker.model.Errors;
import io.temporal.demos.autoscaling.worker.model.Order;
import io.temporal.demos.autoscaling.worker.model.OrderStatus;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Saga;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.time.Duration;
import java.util.Optional;

/**
 * Implements the order processing workflow as a
 * sequential activity pipeline with Saga compensation.
 * Each activity stub is configured with timeouts and
 * retry policies matching its reliability profile.
 */
@WorkflowImpl(taskQueues = OrderWorkflow.TASK_QUEUE)
public class OrderWorkflowImpl implements OrderWorkflow {
    private static final Logger LOGGER = Workflow.getLogger(OrderWorkflowImpl.class);

    private final ValidationActivity validation = Workflow.newActivityStub(
            ValidationActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(5))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(1) // non-retryable: bad input stays bad
                            .build())
                    .build());

    private final InventoryActivity inventory = Workflow.newActivityStub(
            InventoryActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3)
                            .build())
                    .build());

    private final PaymentActivity payment = Workflow.newActivityStub(
            PaymentActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(15))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3)
                            .setDoNotRetry(Errors.INSUFFICIENT_FUNDS_ERROR) // non-retryable type
                            .build())
                    .build());

    private final ShipmentActivity shipment = Workflow.newActivityStub(
            ShipmentActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build());

    private final NotificationActivity notification = Workflow.newActivityStub(
            NotificationActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(5))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(2) // best-effort: not compensated
                            .build())
                    .build());

    private final MetricsActivity metrics = Workflow.newActivityStub(
            MetricsActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(2))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(1)
                            .build())
                    .build());

    private OrderStatus currentStatus = OrderStatus.PENDING;

    @Override
    public OrderStatus getStatus() {
        return currentStatus;
    }

    private void updateStatus(String orderId, OrderStatus status) {
        currentStatus = status;
        metrics.recordOrderStatus(orderId, status);
    }

    @Override
    public Result processOrder(Order order) {
        // Saga tracks compensations (refund, release inventory)
        // and runs them in reverse order if any step fails.
        final var startTime = Workflow.currentTimeMillis();
        final var saga = new Saga(new Saga.Options.Builder().build());
        try {
            MDC.put("orderId", order.orderId());
            LOGGER.atInfo().log("Processing order");

            updateStatus(order.orderId(), OrderStatus.VALIDATING);
            validation.validateOrder(order);
            LOGGER.atInfo().log("Order validated");

            updateStatus(order.orderId(), OrderStatus.RESERVING);
            inventory.reserveInventory(order.items());
            saga.addCompensation(inventory::releaseInventory, order.items());
            LOGGER.atInfo().log("Inventory reserved");

            updateStatus(order.orderId(), OrderStatus.PAYING);
            final var result = payment.processPayment(order.payment());
            saga.addCompensation(payment::refundPayment, result.transactionId());
            LOGGER.atInfo().addKeyValue("transactionId", result.transactionId()).log("Payment processed");

            updateStatus(order.orderId(), OrderStatus.PREPARING);
            final var details = shipment.prepareShipment(order);
            LOGGER.atInfo().addKeyValue("trackingNumber", details.trackingNumber()).log("Shipment prepared");

            updateStatus(order.orderId(), OrderStatus.NOTIFYING);
            notification.sendConfirmation(order, result, details);
            LOGGER.atInfo().log("Notification sent");

            updateStatus(order.orderId(), OrderStatus.COMPLETED);
            metrics.recordOrderDuration(Workflow.currentTimeMillis() - startTime);
            LOGGER.atInfo().log("Order completed");
            return new Result(order.orderId(), OrderStatus.COMPLETED, Optional.empty());
        } catch (Exception e) {
            LOGGER.atError().setCause(e).log("Order failed");
            metrics.recordOrderCompensation();
            updateStatus(order.orderId(), OrderStatus.COMPENSATING);
            saga.compensate();
            updateStatus(order.orderId(), OrderStatus.FAILED);
            final var errorType = e instanceof ActivityFailure af
                    && af.getCause() instanceof ApplicationFailure appF
                    ? appF.getType()
                    : e.getClass().getSimpleName();
            metrics.recordOrderFailure(errorType);
            metrics.recordOrderDuration(Workflow.currentTimeMillis() - startTime);
            return new Result(
                    order.orderId(),
                    OrderStatus.FAILED,
                    Optional.of(new OrderWorkflow.Error(e.getMessage(), errorType)));
        } finally {
            MDC.remove("orderId");
        }
    }
}
