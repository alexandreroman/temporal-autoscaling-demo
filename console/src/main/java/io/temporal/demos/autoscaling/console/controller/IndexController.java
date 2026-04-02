package io.temporal.demos.autoscaling.console.controller;

import io.temporal.api.workflowservice.v1.CountWorkflowExecutionsRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.demos.autoscaling.console.model.Order;
import io.temporal.demos.autoscaling.console.model.PaymentRequest;
import io.temporal.demos.autoscaling.console.workflow.OrderWorkflow;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Controller
public class IndexController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    private static final Map<String, ScenarioProgress> ACTIVE_SCENARIOS =
            new ConcurrentHashMap<>();

    private record ScenarioProgress(int totalBatches, int totalCount,
                                       int batchSize, int delaySeconds,
                                       String preset,
                                       AtomicInteger completedBatches) {
        ScenarioProgress(int totalBatches, int totalCount,
                         int batchSize, int delaySeconds,
                         String preset) {
            this(totalBatches, totalCount, batchSize, delaySeconds,
                    preset, new AtomicInteger(0));
        }

        int percent() {
            return completedBatches.get() * 100 / totalBatches;
        }
    }

    private record Preset(int totalCount, int batchSize, int delaySeconds) {
    }

    private static final Map<String, Preset> PRESETS = Map.of(
            "normal", new Preset(10, 5, 1),
            "load", new Preset(1000, 100, 1)
    );

    private record Product(String sku, String name, BigDecimal unitPrice) {
    }

    private static final List<Product> CATALOG = List.of(
            new Product("SKU-001", "Laptop", new BigDecimal("999.99")),
            new Product("SKU-002", "Keyboard", new BigDecimal("79.99")),
            new Product("SKU-003", "Monitor", new BigDecimal("449.99")),
            new Product("SKU-004", "Mouse", new BigDecimal("39.99")),
            new Product("SKU-005", "Headset", new BigDecimal("129.99")),
            new Product("SKU-006", "Webcam", new BigDecimal("89.99")),
            new Product("SKU-007", "USB Hub", new BigDecimal("34.99")),
            new Product("SKU-008", "Desk Lamp", new BigDecimal("54.99")),
            new Product("SKU-009", "Chair", new BigDecimal("349.99")),
            new Product("SKU-010", "Standing Desk", new BigDecimal("599.99"))
    );

    private static final String RUNNING_ORDER_QUERY =
            "WorkflowType='OrderWorkflow' AND ExecutionStatus='Running'";

    private final WorkflowClient workflowClient;
    private final WorkflowServiceStubs workflowServiceStubs;

    public IndexController(WorkflowClient workflowClient,
                           WorkflowServiceStubs workflowServiceStubs) {
        this.workflowClient = workflowClient;
        this.workflowServiceStubs = workflowServiceStubs;
    }

    @GetMapping("/")
    public String index(Model model) {
        final var defaults = PRESETS.get("normal");
        populateFormModel(model, defaults.totalCount(), defaults.batchSize(),
                defaults.delaySeconds(), "normal");
        return "index";
    }

    @GetMapping("/presets/{name}")
    public String preset(
            @PathVariable String name,
            @RequestParam(required = false) Integer totalCount,
            @RequestParam(required = false) Integer batchSize,
            @RequestParam(required = false) Integer delaySeconds,
            Model model) {
        final var p = PRESETS.get(name);
        if (p != null) {
            populateFormModel(model, p.totalCount(), p.batchSize(),
                    p.delaySeconds(), name);
        } else {
            final var normal = PRESETS.get("normal");
            populateFormModel(model,
                    totalCount != null ? totalCount : normal.totalCount(),
                    batchSize != null ? batchSize : normal.batchSize(),
                    delaySeconds != null ? delaySeconds : normal.delaySeconds(),
                    "custom");
        }
        return "index :: form-content";
    }

    @GetMapping("/plan")
    public String plan(
            @RequestParam int totalCount,
            @RequestParam int batchSize,
            @RequestParam int delaySeconds,
            Model model) {
        populateFormModel(model, totalCount, batchSize, delaySeconds, "custom");
        return "fragments/plan-response";
    }

    private static int batchCount(int totalCount, int batchSize) {
        return (int) Math.ceil((double) totalCount / batchSize);
    }

    private void populateFormModel(Model model, int totalCount, int batchSize,
                                   int delaySeconds, String activePreset) {
        final var batchCount = batchCount(totalCount, batchSize);
        final var lastBatchSize = totalCount % batchSize == 0 ? batchSize : totalCount % batchSize;
        final var totalTime = (batchCount - 1) * delaySeconds;
        final var hasRemainder = lastBatchSize != batchSize && batchCount > 1;

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("batchSize", batchSize);
        model.addAttribute("delaySeconds", delaySeconds);
        model.addAttribute("activePreset", activePreset);
        model.addAttribute("batchCount", batchCount);
        model.addAttribute("lastBatchSize", lastBatchSize);
        model.addAttribute("totalTime", totalTime);
        model.addAttribute("hasRemainder", hasRemainder);
    }

    @PostMapping("/scenarios")
    public String startScenario(
            @RequestParam("totalCount") int totalCount,
            @RequestParam("batchSize") int batchSize,
            @RequestParam("delaySeconds") int delaySeconds,
            @RequestParam("preset") String preset,
            Model model) {

        final var totalBatches = batchCount(totalCount, batchSize);
        final var scenarioId = UUID.randomUUID().toString().substring(0, 8);

        final var progress = new ScenarioProgress(
                totalBatches, totalCount, batchSize,
                delaySeconds, preset);
        ACTIVE_SCENARIOS.put(scenarioId, progress);

        LOGGER.atInfo()
                .addKeyValue("scenarioId", scenarioId)
                .addKeyValue("preset", preset)
                .addKeyValue("totalCount", totalCount)
                .addKeyValue("batchSize", batchSize)
                .addKeyValue("delaySeconds", delaySeconds)
                .addKeyValue("totalBatches", totalBatches)
                .log("Scenario requested");

        Thread.startVirtualThread(() -> {
            var launched = 0;
            var batchNumber = 1;
            while (launched < totalCount) {
                if (batchNumber > 1) {
                    try {
                        Thread.sleep((long) delaySeconds * 1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.atWarn()
                                .addKeyValue("scenarioId", scenarioId)
                                .addKeyValue("preset", preset)
                                .addKeyValue("launched", launched)
                                .log("Background batch scheduling interrupted");
                        ACTIVE_SCENARIOS.remove(scenarioId);
                        return;
                    }
                }
                final var size = Math.min(batchSize, totalCount - launched);
                launchBatch(size, preset, batchNumber, totalBatches);
                progress.completedBatches().incrementAndGet();
                launched += size;
                batchNumber++;
            }
        });

        populateFormModel(model, totalCount, batchSize,
                delaySeconds, preset);
        model.addAttribute("disabled", true);
        model.addAttribute("scenarioId", scenarioId);
        model.addAttribute("completedBatches", 0);
        model.addAttribute("totalBatches", totalBatches);
        model.addAttribute("percent", 0);
        return "fragments/scenario-started";
    }

    private void launchBatch(int size, String preset, int batchNumber, int totalBatches) {
        LOGGER.atInfo()
                .addKeyValue("preset", preset)
                .addKeyValue("batchNumber", batchNumber)
                .addKeyValue("totalBatches", totalBatches)
                .addKeyValue("size", size)
                .log("Launching batch");

        for (var i = 0; i < size; i++) {
            final var order = generateOrder();
            final var options = WorkflowOptions.newBuilder()
                    .setTaskQueue(OrderWorkflow.TASK_QUEUE)
                    .setWorkflowId("order-" + order.orderId())
                    .build();
            final var stub = workflowClient.newWorkflowStub(OrderWorkflow.class, options);
            WorkflowClient.start(stub::processOrder, order);
        }
    }

    private Order generateOrder() {
        final var random = ThreadLocalRandom.current();
        final var orderId = UUID.randomUUID().toString().substring(0, 8);
        final var customerId = String.format("%04d", random.nextInt(1_000, 10_000));

        final var itemCount = random.nextInt(1, 6);
        final var items = IntStream.range(0, itemCount)
                .mapToObj(_ -> {
                    final var product = CATALOG.get(random.nextInt(CATALOG.size()));
                    final var quantity = random.nextInt(1, 6);
                    return new Order.Item(product.sku(), product.name(), quantity, product.unitPrice());
                })
                .toList();

        final var totalAmount = items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var payment = new PaymentRequest("CARD", totalAmount, "EUR");
        return new Order(orderId, customerId, items, payment);
    }

    @GetMapping("/workflows/running-count")
    public String runningCount(Model model) {
        final var request = CountWorkflowExecutionsRequest.newBuilder()
                .setNamespace("default")
                .setQuery(RUNNING_ORDER_QUERY)
                .build();
        final var response = workflowServiceStubs.blockingStub()
                .countWorkflowExecutions(request);
        model.addAttribute("runningCount", response.getCount());
        return "fragments/running-count";
    }

    @GetMapping("/scenarios/{id}/progress")
    public String scenarioProgress(@PathVariable String id, Model model) {
        final var progress = ACTIVE_SCENARIOS.get(id);
        if (progress == null) {
            return "fragments/progress-complete";
        }

        final var completed = progress.completedBatches().get();
        if (completed >= progress.totalBatches()) {
            ACTIVE_SCENARIOS.remove(id);
            populateFormModel(model, progress.totalCount(),
                    progress.batchSize(), progress.delaySeconds(),
                    progress.preset());
            return "fragments/progress-complete";
        }

        model.addAttribute("scenarioId", id);
        model.addAttribute("completedBatches", completed);
        model.addAttribute("totalBatches", progress.totalBatches());
        model.addAttribute("percent", progress.percent());
        return "index :: progress-bar";
    }

}
