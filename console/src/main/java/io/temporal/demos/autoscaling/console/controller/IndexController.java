package io.temporal.demos.autoscaling.console.controller;

import io.temporal.demos.autoscaling.console.model.Preset;
import io.temporal.demos.autoscaling.console.service.ScenarioService;
import io.temporal.demos.autoscaling.console.service.ScenarioService.ScenarioProgress;
import io.temporal.demos.autoscaling.console.service.WorkflowCountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class IndexController {

    private final ScenarioService scenarioService;
    private final WorkflowCountService workflowCountService;

    public IndexController(ScenarioService scenarioService,
                           WorkflowCountService workflowCountService) {
        this.scenarioService = scenarioService;
        this.workflowCountService = workflowCountService;
    }

    @GetMapping("/")
    public String index(Model model) {
        final var active = scenarioService.findActive();
        if (active.isPresent()) {
            final var entry = active.get();
            final var progress = entry.getValue();
            populateFormModel(model, progress.totalCount(), progress.batchSize(),
                    progress.delaySeconds(), progress.preset());
            populateProgressModel(model, entry.getKey(), progress);
            return "index";
        }

        final var defaults = Preset.DEFAULTS.get(Preset.NORMAL);
        populateFormModel(model, defaults.totalCount(), defaults.batchSize(),
                defaults.delaySeconds(), Preset.NORMAL);
        return "index";
    }

    @GetMapping("/presets/{name}")
    public String preset(
            @PathVariable String name,
            @RequestParam(required = false) Integer totalCount,
            @RequestParam(required = false) Integer batchSize,
            @RequestParam(required = false) Integer delaySeconds,
            Model model) {
        final var p = Preset.DEFAULTS.get(name);
        if (p != null) {
            populateFormModel(model, p.totalCount(), p.batchSize(),
                    p.delaySeconds(), name);
        } else {
            final var normal = Preset.DEFAULTS.get(Preset.NORMAL);
            populateFormModel(model,
                    totalCount != null ? totalCount : normal.totalCount(),
                    batchSize != null ? batchSize : normal.batchSize(),
                    delaySeconds != null ? delaySeconds : normal.delaySeconds(),
                    Preset.CUSTOM);
        }
        return "index :: form-content";
    }

    @GetMapping("/plan")
    public String plan(
            @RequestParam int totalCount,
            @RequestParam int batchSize,
            @RequestParam int delaySeconds,
            Model model) {
        populateFormModel(model, totalCount, batchSize, delaySeconds, Preset.CUSTOM);
        return "fragments/plan-response";
    }

    @PostMapping("/scenarios")
    public String startScenario(
            @RequestParam("totalCount") int totalCount,
            @RequestParam("batchSize") int batchSize,
            @RequestParam("delaySeconds") int delaySeconds,
            @RequestParam("preset") String preset,
            Model model) {

        final var scenarioId = scenarioService.start(
                totalCount, batchSize, delaySeconds, preset
        );

        populateFormModel(model, totalCount, batchSize, delaySeconds, preset);
        final var progress = scenarioService.get(scenarioId).orElseThrow();
        populateProgressModel(model, scenarioId, progress);
        return "index :: form-content";
    }

    @GetMapping("/workflows/running-count")
    public String runningCount(Model model) {
        model.addAttribute("runningCount", workflowCountService.countRunning());
        return "fragments/running-count";
    }

    @GetMapping("/scenarios/{id}/progress")
    public String scenarioProgress(
            @PathVariable String id,
            Model model,
            HttpServletResponse response) {
        final var maybeProgress = scenarioService.get(id);
        if (maybeProgress.isEmpty()) {
            return "index :: launch-button";
        }

        final var progress = maybeProgress.get();
        if (progress.isComplete()) {
            scenarioService.remove(id);
            populateFormModel(model, progress.totalCount(),
                    progress.batchSize(), progress.delaySeconds(),
                    progress.preset());
            response.setHeader("HX-Retarget", "#form-content");
            response.setHeader("HX-Reswap", "outerHTML");
            return "index :: form-content";
        }

        model.addAttribute("scenarioId", id);
        model.addAttribute("completedBatches", progress.completedCount());
        model.addAttribute("totalBatches", progress.totalBatches());
        model.addAttribute("percent", progress.percent());
        return "index :: progress-bar";
    }

    private void populateFormModel(Model model, int totalCount, int batchSize,
                                   int delaySeconds, String activePreset) {
        final var batchCount = Preset.batchCount(totalCount, batchSize);
        final var lastBatchSize =
                totalCount % batchSize == 0 ? batchSize : totalCount % batchSize;
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

    private void populateProgressModel(Model model, String scenarioId,
                                       ScenarioProgress progress) {
        model.addAttribute("disabled", true);
        model.addAttribute("scenarioId", scenarioId);
        model.addAttribute("completedBatches", progress.completedCount());
        model.addAttribute("totalBatches", progress.totalBatches());
        model.addAttribute("percent", progress.percent());
    }
}
