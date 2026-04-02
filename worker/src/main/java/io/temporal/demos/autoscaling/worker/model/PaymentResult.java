package io.temporal.demos.autoscaling.worker.model;

public record PaymentResult(
        String transactionId,
        double amount,
        String currency
) {
}
