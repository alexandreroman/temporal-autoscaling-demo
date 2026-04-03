package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.demos.autoscaling.worker.model.Order;
import io.temporal.demos.autoscaling.worker.model.PaymentResult;
import io.temporal.demos.autoscaling.worker.model.ShipmentDetails;

/**
 * Sends order confirmation notifications to customers.
 * Best-effort: limited retries and not compensated
 * on failure, since a missed notification does not
 * affect order correctness.
 */
@ActivityInterface
public interface NotificationActivity {
    @ActivityMethod
    void sendConfirmation(Order order, PaymentResult payment, ShipmentDetails shipment);
}
