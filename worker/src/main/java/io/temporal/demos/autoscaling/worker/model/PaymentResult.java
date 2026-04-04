package io.temporal.demos.autoscaling.worker.model;

import java.math.BigDecimal;

public record PaymentResult(
        String transactionId,
        BigDecimal amount,
        String currency
) {
}
