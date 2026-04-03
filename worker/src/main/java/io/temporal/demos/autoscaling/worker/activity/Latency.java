package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.demos.autoscaling.worker.model.Errors;
import io.temporal.failure.ApplicationFailure;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates external service latency for demo purposes.
 * Each activity call sleeps for a random duration within
 * the given range, mimicking real-world I/O delays.
 */
final class Latency {
    private Latency() {}

    static void simulate(int minMs, int maxMs) {
        final var duration = ThreadLocalRandom.current().nextInt(minMs, maxMs + 1);
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ApplicationFailure.newFailure("Activity interrupted", Errors.INTERRUPTED_ERROR);
        }
    }
}
