package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.demos.autoscaling.worker.model.PaymentRequest;
import io.temporal.demos.autoscaling.worker.model.PaymentResult;

@ActivityInterface
public interface PaymentActivity {
    @ActivityMethod
    PaymentResult processPayment(PaymentRequest request);

    @ActivityMethod
    void refundPayment(String transactionId);
}
