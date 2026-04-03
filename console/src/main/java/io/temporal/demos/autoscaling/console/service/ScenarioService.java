package io.temporal.demos.autoscaling.console.service;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.demos.autoscaling.console.model.Preset;
import io.temporal.demos.autoscaling.console.workflow.OrderWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ScenarioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioService.class);

    private final Map<String, ScenarioProgress> activeScenarios = new ConcurrentHashMap<>();
    // Volatile: written by request threads, read by findActive()
    // on different threads without synchronization.
    private volatile String latestScenarioId;

    private final WorkflowClient workflowClient;
    private final OrderFactory orderFactory;

    public ScenarioService(WorkflowClient workflowClient, OrderFactory orderFactory) {
        this.workflowClient = workflowClient;
        this.orderFactory = orderFactory;
    }

    public record ScenarioProgress(
            int totalBatches, int totalCount, int batchSize,
            int delaySeconds, String preset,
            AtomicInteger completedBatches
    ) {
        ScenarioProgress(int totalBatches, int totalCount,
                         int batchSize, int delaySeconds, String preset) {
            this(totalBatches, totalCount, batchSize, delaySeconds,
                    preset, new AtomicInteger(0));
        }

        public int completedCount() {
            return completedBatches.get();
        }

        public boolean isComplete() {
            return completedBatches.get() >= totalBatches;
        }

        public int percent() {
            return completedBatches.get() * 100 / totalBatches;
        }
    }

    public String start(int totalCount, int batchSize, int delaySeconds, String preset) {
        activeScenarios.entrySet().removeIf(e -> e.getValue().isComplete());

        final var totalBatches = Preset.batchCount(totalCount, batchSize);
        final var scenarioId = UUID.randomUUID().toString().substring(0, 8);

        final var progress = new ScenarioProgress(
                totalBatches, totalCount, batchSize, delaySeconds, preset
        );
        activeScenarios.put(scenarioId, progress);
        latestScenarioId = scenarioId;

        LOGGER.atInfo()
                .addKeyValue("scenarioId", scenarioId)
                .addKeyValue("preset", preset)
                .addKeyValue("totalCount", totalCount)
                .addKeyValue("batchSize", batchSize)
                .addKeyValue("delaySeconds", delaySeconds)
                .addKeyValue("totalBatches", totalBatches)
                .log("Scenario requested");

        Thread.startVirtualThread(() -> runScenario(scenarioId, progress));

        return scenarioId;
    }

    public Optional<Map.Entry<String, ScenarioProgress>> findActive() {
        final var id = latestScenarioId;
        if (id == null) {
            return Optional.empty();
        }
        final var progress = activeScenarios.get(id);
        if (progress == null || progress.isComplete()) {
            return Optional.empty();
        }
        return Optional.of(Map.entry(id, progress));
    }

    public Optional<ScenarioProgress> get(String id) {
        return Optional.ofNullable(activeScenarios.get(id));
    }

    public void remove(String id) {
        activeScenarios.remove(id);
    }

    private void runScenario(String scenarioId, ScenarioProgress progress) {
        final var totalCount = progress.totalCount();
        final var batchSize = progress.batchSize();
        final var totalBatches = progress.totalBatches();
        final var preset = progress.preset();
        var launched = 0;
        var batchNumber = 1;
        while (launched < totalCount) {
            if (batchNumber > 1) {
                try {
                    Thread.sleep((long) progress.delaySeconds() * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.atWarn()
                            .addKeyValue("scenarioId", scenarioId)
                            .addKeyValue("preset", preset)
                            .addKeyValue("launched", launched)
                            .log("Background batch scheduling interrupted");
                    activeScenarios.remove(scenarioId);
                    return;
                }
            }
            final var size = Math.min(batchSize, totalCount - launched);
            launchBatch(size, preset, batchNumber, totalBatches);
            progress.completedBatches().incrementAndGet();
            launched += size;
            batchNumber++;
        }
    }

    private void launchBatch(int size, String preset, int batchNumber, int totalBatches) {
        LOGGER.atInfo()
                .addKeyValue("preset", preset)
                .addKeyValue("batchNumber", batchNumber)
                .addKeyValue("totalBatches", totalBatches)
                .addKeyValue("size", size)
                .log("Launching batch");

        for (var i = 0; i < size; i++) {
            final var order = orderFactory.create();
            final var options = WorkflowOptions.newBuilder()
                    .setTaskQueue(OrderWorkflow.TASK_QUEUE)
                    .setWorkflowId("order-" + order.orderId())
                    .build();
            final var stub = workflowClient.newWorkflowStub(OrderWorkflow.class, options);
            WorkflowClient.start(stub::processOrder, order);
        }
    }
}
