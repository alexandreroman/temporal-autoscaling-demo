package io.temporal.demos.autoscaling.console.model;

import java.math.BigDecimal;

public record Product(String sku, String name, BigDecimal unitPrice) {
}
