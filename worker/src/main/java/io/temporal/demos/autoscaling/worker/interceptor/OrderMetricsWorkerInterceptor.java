package io.temporal.demos.autoscaling.worker.interceptor;

import com.uber.m3.tally.Scope;
import com.uber.m3.util.Duration;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptorBase;
import io.temporal.common.interceptors.WorkerInterceptorBase;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptorBase;
import io.temporal.demos.autoscaling.worker.model.OrderStatus;
import io.temporal.demos.autoscaling.worker.workflow.OrderWorkflow;
import io.temporal.workflow.Workflow;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
class OrderMetricsWorkerInterceptor extends WorkerInterceptorBase {

    private static final String WORKFLOW_TYPE = "OrderWorkflow";

    private final MeterRegistry registry;

    OrderMetricsWorkerInterceptor(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public WorkflowInboundCallsInterceptor interceptWorkflow(
            WorkflowInboundCallsInterceptor next) {
        return new WorkflowMetricsInterceptor(next);
    }

    @Override
    public ActivityInboundCallsInterceptor interceptActivity(
            ActivityInboundCallsInterceptor next) {
        return new ActivityMetricsInterceptor(registry, next);
    }

    private static class WorkflowMetricsInterceptor
            extends WorkflowInboundCallsInterceptorBase {

        WorkflowMetricsInterceptor(WorkflowInboundCallsInterceptor next) {
            super(next);
        }

        @Override
        public WorkflowOutput execute(WorkflowInput input) {
            final var startTime = Workflow.currentTimeMillis();
            try {
                final var output = super.execute(input);
                final var scope = Workflow.getMetricsScope();
                recordFailureMetrics(scope, output);
                return output;
            } finally {
                final var elapsed = Workflow.currentTimeMillis() - startTime;
                final var scope = Workflow.getMetricsScope();
                scope.timer("order.duration")
                        .record(Duration.ofMillis(elapsed));
            }
        }

        private void recordFailureMetrics(Scope scope, WorkflowOutput output) {
            final var workflowType =
                    Workflow.getInfo().getWorkflowType();
            if (!WORKFLOW_TYPE.equals(workflowType)) {
                return;
            }

            final var result = (OrderWorkflow.Result) output.getResult();
            if (result.status() != OrderStatus.FAILED) {
                return;
            }

            scope.counter("order.compensation").inc(1);
            result.error().ifPresent(error ->
                    scope.tagged(Map.of("errorType", error.type()))
                            .counter("order.failure")
                            .inc(1));
        }
    }

    private static class ActivityMetricsInterceptor
            extends ActivityInboundCallsInterceptorBase {

        private final MeterRegistry registry;
        private String activityType;

        ActivityMetricsInterceptor(
                MeterRegistry registry,
                ActivityInboundCallsInterceptor next) {
            super(next);
            this.registry = registry;
        }

        @Override
        public void init(ActivityExecutionContext context) {
            this.activityType = context.getInfo().getActivityType();
            super.init(context);
        }

        @Override
        public ActivityOutput execute(ActivityInput input) {
            final var sample = Timer.start(registry);
            try {
                return super.execute(input);
            } finally {
                sample.stop(registry.timer(
                        "order.activity.duration",
                        "activity", activityType));
            }
        }
    }
}
