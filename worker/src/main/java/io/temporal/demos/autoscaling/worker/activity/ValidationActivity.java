package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.demos.autoscaling.worker.model.Order;

@ActivityInterface
public interface ValidationActivity {
    @ActivityMethod
    void validateOrder(Order order);
}
