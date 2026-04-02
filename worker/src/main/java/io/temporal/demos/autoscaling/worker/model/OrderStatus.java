package io.temporal.demos.autoscaling.worker.model;

import com.fasterxml.jackson.databind.EnumNamingStrategies;
import com.fasterxml.jackson.databind.annotation.EnumNaming;

@EnumNaming(EnumNamingStrategies.UpperCamelCaseStrategy.class)
public enum OrderStatus {
    PENDING, VALIDATING, RESERVING,
    PAYING, PREPARING, NOTIFYING,
    COMPLETED, FAILED, COMPENSATING
}
