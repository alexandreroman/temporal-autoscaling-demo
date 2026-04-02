package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.demos.autoscaling.worker.model.Errors;
import io.temporal.demos.autoscaling.worker.model.PaymentRequest;
import io.temporal.demos.autoscaling.worker.model.PaymentResult;
import io.temporal.demos.autoscaling.worker.workflow.OrderWorkflow;
import io.temporal.failure.ApplicationFailure;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ActivityImpl(taskQueues = OrderWorkflow.TASK_QUEUE)
class PaymentActivityImpl implements PaymentActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentActivityImpl.class);

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        LOGGER.atInfo()
                .addKeyValue("method", request.method())
                .addKeyValue("amount", request.amount())
                .addKeyValue("currency", request.currency())
                .log("Processing payment");

        Latency.simulate(1500, 3000);

        if (Math.random() < 0.05) {
            throw ApplicationFailure.newFailure("Gateway timeout", Errors.GATEWAY_TIMEOUT_ERROR);
        }

        // 2% insufficient funds: non-retryable, triggers Saga
        if (Math.random() < 0.02) {
            throw ApplicationFailure.newNonRetryableFailure(
                    "Insufficient funds", Errors.INSUFFICIENT_FUNDS_ERROR);
        }

        final var transactionId = UUID.randomUUID().toString();

        LOGGER.atInfo()
                .addKeyValue("transactionId", transactionId)
                .addKeyValue("amount", request.amount())
                .addKeyValue("currency", request.currency())
                .log("Payment processed");

        return new PaymentResult(transactionId, request.amount().doubleValue(), request.currency());
    }

    @Override
    public void refundPayment(String transactionId) {
        LOGGER.atInfo()
                .addKeyValue("transactionId", transactionId)
                .log("Processing refund (compensation)");

        Latency.simulate(300, 800);

        LOGGER.atInfo()
                .addKeyValue("transactionId", transactionId)
                .log("Refund completed");
    }
}
