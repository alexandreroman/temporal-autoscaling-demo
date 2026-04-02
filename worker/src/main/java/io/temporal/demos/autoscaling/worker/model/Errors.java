package io.temporal.demos.autoscaling.worker.model;

public final class Errors {
    private Errors() {}

    public static final String VALIDATION_ERROR = "ValidationError";
    public static final String INTERRUPTED_ERROR = "InterruptedError";
    public static final String INSUFFICIENT_FUNDS_ERROR = "InsufficientFundsError";
    public static final String GATEWAY_TIMEOUT_ERROR = "GatewayTimeoutError";
}
