package io.temporal.demos.autoscaling.console.service;

import io.temporal.api.workflowservice.v1.CountWorkflowExecutionsRequest;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WorkflowCountService {

    private static final String RUNNING_ORDER_QUERY =
            "WorkflowType='OrderWorkflow' AND ExecutionStatus='Running'";

    private final WorkflowServiceStubs workflowServiceStubs;
    private final String namespace;

    public WorkflowCountService(WorkflowServiceStubs workflowServiceStubs,
                                @Value("${spring.temporal.namespace}") String namespace) {
        this.workflowServiceStubs = workflowServiceStubs;
        this.namespace = namespace;
    }

    public long countRunning() {
        final var request = CountWorkflowExecutionsRequest.newBuilder()
                .setNamespace(namespace)
                .setQuery(RUNNING_ORDER_QUERY)
                .build();
        final var response = workflowServiceStubs.blockingStub()
                .countWorkflowExecutions(request);
        return response.getCount();
    }
}
