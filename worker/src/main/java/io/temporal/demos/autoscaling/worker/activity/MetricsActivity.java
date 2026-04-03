package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.demos.autoscaling.worker.model.OrderStatus;

/**
 * Records order metrics for observability: status
 * transitions, durations, failures, and compensations.
 * Best-effort, non-retryable: a missed metric does not
 * affect order correctness.
 */
@ActivityInterface
public interface MetricsActivity {
    @ActivityMethod
    void recordOrderStatus(String orderId, OrderStatus status);

    @ActivityMethod
    void recordOrderDuration(long durationMs);

    @ActivityMethod
    void recordOrderFailure(String errorType);

    @ActivityMethod
    void recordOrderCompensation();
}
