package io.temporal.demos.autoscaling.worker.activity;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.temporal.demos.autoscaling.worker.model.Errors;
import io.temporal.demos.autoscaling.worker.model.Order;
import io.temporal.demos.autoscaling.worker.workflow.OrderWorkflow;
import io.temporal.failure.ApplicationFailure;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ActivityImpl(taskQueues = OrderWorkflow.TASK_QUEUE)
class ValidationActivityImpl implements ValidationActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationActivityImpl.class);
    private final MeterRegistry registry;

    ValidationActivityImpl(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void validateOrder(Order order) {
        final var sample = Timer.start(registry);
        try {
            LOGGER.atInfo()
                    .addKeyValue("orderId", order.orderId())
                    .addKeyValue("itemCount", order.items() != null ? order.items().size() : 0)
                    .log("Validating order");

            if (order.orderId() == null || order.orderId().isBlank()) {
                throw ApplicationFailure.newNonRetryableFailure(
                        "Order ID must not be blank", Errors.VALIDATION_ERROR);
            }

            if (order.items() == null || order.items().isEmpty()) {
                throw ApplicationFailure.newNonRetryableFailure(
                        "Order must have at least one item", Errors.VALIDATION_ERROR);
            }

            if (order.payment() == null
                    || order.payment().amount() == null
                    || order.payment().amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw ApplicationFailure.newNonRetryableFailure(
                        "Payment amount must be positive", Errors.VALIDATION_ERROR);
            }

            Latency.simulate(200, 500);

            LOGGER.atInfo().addKeyValue("orderId", order.orderId()).log("Order validation passed");
        } finally {
            sample.stop(registry.timer("order.activity.duration", "activity", "Validation"));
        }
    }
}
