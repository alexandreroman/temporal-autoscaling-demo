package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.demos.autoscaling.worker.model.Order;

/**
 * Validates order data before processing.
 * Rejects orders with missing IDs, empty item lists,
 * or non-positive payment amounts. Failures are
 * non-retryable since invalid input will not self-correct.
 */
@ActivityInterface
public interface ValidationActivity {
    @ActivityMethod
    void validateOrder(Order order);
}
