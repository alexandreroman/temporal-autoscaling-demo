package io.temporal.demos.autoscaling.console.model;

import java.util.Map;

public record Preset(int totalCount, int batchSize, int delaySeconds) {

    public static final Map<String, Preset> DEFAULTS = Map.of(
            "normal", new Preset(10, 5, 1),
            "load", new Preset(1000, 100, 1)
    );

    public static int batchCount(int totalCount, int batchSize) {
        return (int) Math.ceil((double) totalCount / batchSize);
    }
}
