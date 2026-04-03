package io.temporal.demos.autoscaling.worker.activity;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.temporal.demos.autoscaling.worker.model.OrderStatus;
import io.temporal.demos.autoscaling.worker.workflow.OrderWorkflow;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = OrderWorkflow.TASK_QUEUE)
class MetricsActivityImpl implements MetricsActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsActivityImpl.class);

    private final MeterRegistry registry;

    MetricsActivityImpl(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void recordOrderStatus(String orderId, OrderStatus status) {
        final var statusLabel = status.label();
        registry.counter("order.status", "status", statusLabel).increment();
        LOGGER.atDebug()
                .addKeyValue("orderId", orderId)
                .addKeyValue("status", statusLabel)
                .log("Order status recorded");
    }

    @Override
    public void recordOrderDuration(long durationMs) {
        registry.timer("order.duration").record(durationMs, TimeUnit.MILLISECONDS);
        LOGGER.atDebug()
                .addKeyValue("durationMs", durationMs)
                .log("Order duration recorded");
    }

    @Override
    public void recordOrderFailure(String errorType) {
        registry.counter("order.failure", "errorType", errorType).increment();
        LOGGER.atDebug()
                .addKeyValue("errorType", errorType)
                .log("Order failure recorded");
    }

    @Override
    public void recordOrderCompensation() {
        registry.counter("order.compensation").increment();
        LOGGER.atDebug()
                .log("Order compensation recorded");
    }
}
