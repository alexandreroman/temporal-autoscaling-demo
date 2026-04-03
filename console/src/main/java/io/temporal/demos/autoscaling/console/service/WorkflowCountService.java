package io.temporal.demos.autoscaling.console.service;

import io.temporal.api.workflowservice.v1.CountWorkflowExecutionsRequest;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.stereotype.Service;

@Service
public class WorkflowCountService {

    private static final String RUNNING_ORDER_QUERY =
            "WorkflowType='OrderWorkflow' AND ExecutionStatus='Running'";

    private final WorkflowServiceStubs workflowServiceStubs;

    public WorkflowCountService(WorkflowServiceStubs workflowServiceStubs) {
        this.workflowServiceStubs = workflowServiceStubs;
    }

    public long countRunning() {
        final var request = CountWorkflowExecutionsRequest.newBuilder()
                .setNamespace("default")
                .setQuery(RUNNING_ORDER_QUERY)
                .build();
        final var response = workflowServiceStubs.blockingStub()
                .countWorkflowExecutions(request);
        return response.getCount();
    }
}
