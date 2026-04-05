package io.temporal.demos.autoscaling.console.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnExpression("!'${grafana.url:}'.isBlank()")
public class GrafanaAnnotationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrafanaAnnotationService.class);

    private final RestClient restClient;

    public GrafanaAnnotationService(@Value("${grafana.url}") String grafanaUrl,
                                    @Value("${grafana.api-key:}") String apiKey) {
        final var builder = RestClient.builder().baseUrl(grafanaUrl);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + apiKey);
        }
        this.restClient = builder.build();
    }

    public void annotate(String preset, int totalCount, int batchSize, int delaySeconds) {
        final var text = "Scenario launched: %d workflows, %d/batch, %ds delay (%s)"
                .formatted(totalCount, batchSize, delaySeconds, preset);
        final var body = Map.of(
                "text", text,
                "tags", List.of("scenario", preset)
        );
        try {
            restClient.post()
                    .uri("/api/annotations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            LOGGER.atInfo()
                    .addKeyValue("preset", preset)
                    .addKeyValue("totalCount", totalCount)
                    .log("Grafana annotation created");
        } catch (Exception e) {
            LOGGER.atWarn()
                    .addKeyValue("preset", preset)
                    .addKeyValue("error", e.getMessage())
                    .log("Failed to create Grafana annotation", e);
        }
    }
}
