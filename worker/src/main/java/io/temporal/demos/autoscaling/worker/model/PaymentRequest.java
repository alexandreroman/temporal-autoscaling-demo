package io.temporal.demos.autoscaling.worker.model;

import java.math.BigDecimal;

public record PaymentRequest(
        String method,      // "CARD", "PAYPAL", ...
        BigDecimal amount,
        String currency     // "EUR", "USD", ...
) {
}
