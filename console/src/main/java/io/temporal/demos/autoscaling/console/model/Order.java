package io.temporal.demos.autoscaling.console.model;

import java.math.BigDecimal;
import java.util.List;

public record Order(
        String orderId,
        String customerId,
        List<Item> items,
        PaymentRequest payment
) {
    public record Item(
            String sku,
            String label,
            int quantity,
            BigDecimal unitPrice
    ) {
    }
}
