package io.temporal.demos.autoscaling.console.model;

import java.math.BigDecimal;

public record PaymentRequest(
        String method,
        BigDecimal amount,
        String currency
) {
}
