package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.demos.autoscaling.worker.model.Order;
import io.temporal.demos.autoscaling.worker.model.ShipmentDetails;

@ActivityInterface
public interface ShipmentActivity {
    @ActivityMethod
    ShipmentDetails prepareShipment(Order order);
}
