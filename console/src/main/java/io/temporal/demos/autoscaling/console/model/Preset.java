package io.temporal.demos.autoscaling.console.model;

import java.util.Map;

public record Preset(int totalCount, int batchSize, int delaySeconds) {

    public static final String NORMAL = "normal";
    public static final String LOAD = "load";
    public static final String CUSTOM = "custom";

    public static final Map<String, Preset> DEFAULTS = Map.of(
            NORMAL, new Preset(10, 5, 1),
            LOAD, new Preset(1000, 100, 1)
    );

    public static int batchCount(int totalCount, int batchSize) {
        return (int) Math.ceil((double) totalCount / batchSize);
    }
}
