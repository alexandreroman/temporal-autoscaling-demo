package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.demos.autoscaling.worker.model.PaymentRequest;
import io.temporal.demos.autoscaling.worker.model.PaymentResult;

/**
 * Handles payment processing and refunds.
 * {@link #processPayment} may throw a non-retryable
 * {@code InsufficientFundsError} that triggers Saga
 * compensation. {@link #refundPayment} is the
 * corresponding compensation action.
 */
@ActivityInterface
public interface PaymentActivity {
    @ActivityMethod
    PaymentResult processPayment(PaymentRequest request);

    @ActivityMethod
    void refundPayment(String transactionId);
}
