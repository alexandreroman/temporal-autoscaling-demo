package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.demos.autoscaling.worker.model.Order;
import io.temporal.demos.autoscaling.worker.model.PaymentResult;
import io.temporal.demos.autoscaling.worker.model.ShipmentDetails;

@ActivityInterface
public interface NotificationActivity {
    @ActivityMethod
    void sendConfirmation(Order order, PaymentResult payment, ShipmentDetails shipment);
}
