package io.temporal.demos.autoscaling.worker.model;

import java.time.LocalDate;

public record ShipmentDetails(
        String trackingNumber,
        String carrier,
        LocalDate estimatedDelivery
) {
}
