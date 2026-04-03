package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.demos.autoscaling.worker.model.Order;
import io.temporal.demos.autoscaling.worker.model.ShipmentDetails;

/**
 * Prepares shipment details for a completed order.
 * Assigns a tracking number, selects a carrier,
 * and computes an estimated delivery date.
 */
@ActivityInterface
public interface ShipmentActivity {
    @ActivityMethod
    ShipmentDetails prepareShipment(Order order);
}
