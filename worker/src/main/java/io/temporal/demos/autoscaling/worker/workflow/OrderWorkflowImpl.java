package io.temporal.demos.autoscaling.worker.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
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

    @Override
    public Result processOrder(Order order) {
        // Saga tracks compensations (refund, release inventory)
        // and runs them in reverse order if any step fails.
        final var saga = new Saga(new Saga.Options.Builder().build());
        try {
            MDC.put("orderId", order.orderId());
            LOGGER.atInfo().log("Processing order");

            validation.validateOrder(order);
            LOGGER.atInfo().log("Order validated");

            inventory.reserveInventory(order.items());
            saga.addCompensation(inventory::releaseInventory, order.items());
            LOGGER.atInfo().log("Inventory reserved");

            final var result = payment.processPayment(order.payment());
            saga.addCompensation(payment::refundPayment, result.transactionId());
            LOGGER.atInfo().addKeyValue("transactionId", result.transactionId()).log("Payment processed");

            final var details = shipment.prepareShipment(order);
            LOGGER.atInfo().addKeyValue("trackingNumber", details.trackingNumber()).log("Shipment prepared");

            notification.sendConfirmation(order, result, details);
            LOGGER.atInfo().log("Notification sent");

            LOGGER.atInfo().log("Order completed");
            return new Result(order.orderId(), OrderStatus.COMPLETED, Optional.empty());
        } catch (Exception e) {
            LOGGER.atError().setCause(e).log("Order failed");
            saga.compensate();
            return new Result(order.orderId(), OrderStatus.FAILED, Optional.of(e.getMessage()));
        } finally {
            MDC.remove("orderId");
        }
    }
}
