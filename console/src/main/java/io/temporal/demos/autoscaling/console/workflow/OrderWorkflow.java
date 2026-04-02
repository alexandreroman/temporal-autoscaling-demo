package io.temporal.demos.autoscaling.console.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.temporal.demos.autoscaling.console.model.Order;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.Optional;

@WorkflowInterface
public interface OrderWorkflow {
    String TASK_QUEUE = "order-processing";

    @WorkflowMethod
    Result processOrder(Order order);

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    record Result(
            String orderId,
            String status,
            Optional<String> errorMessage
    ) {
    }
}
