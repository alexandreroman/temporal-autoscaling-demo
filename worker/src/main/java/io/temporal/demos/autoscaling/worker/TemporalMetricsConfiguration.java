package io.temporal.demos.autoscaling.worker;

import com.uber.m3.tally.RootScopeBuilder;
import com.uber.m3.tally.Scope;
import com.uber.m3.util.Duration;
import io.micrometer.core.instrument.MeterRegistry;
import io.temporal.common.reporter.MicrometerClientStatsReporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Workaround: Temporal Spring Boot Starter 1.33.0
// references a Spring Boot 3 auto-configuration class
// in @AutoConfigureAfter, breaking bean ordering on
// Spring Boot 4. This manually wires the metrics bridge.
@Configuration
class TemporalMetricsConfiguration {
    private static final Duration REPORT_INTERVAL =
            Duration.ofSeconds(10);

    @Bean
    Scope temporalMetricsScope(MeterRegistry registry) {
        final var reporter = new MicrometerClientStatsReporter(registry);
        return new RootScopeBuilder().reporter(reporter).reportEvery(REPORT_INTERVAL);
    }
}
