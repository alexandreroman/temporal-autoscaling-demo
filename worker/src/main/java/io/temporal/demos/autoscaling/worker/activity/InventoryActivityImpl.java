package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.demos.autoscaling.worker.model.Order;
import io.temporal.demos.autoscaling.worker.workflow.OrderWorkflow;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ActivityImpl(taskQueues = OrderWorkflow.TASK_QUEUE)
class InventoryActivityImpl implements InventoryActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryActivityImpl.class);

    @Override
    public void reserveInventory(List<Order.Item> items) {
        LOGGER.atInfo().addKeyValue("itemCount", items.size()).log("Reserving inventory");
        Latency.simulate(300, 800);
        LOGGER.atInfo().addKeyValue("itemCount", items.size()).log("Inventory reserved");
    }

    @Override
    public void releaseInventory(List<Order.Item> items) {
        LOGGER.atInfo().addKeyValue("itemCount", items.size()).log("Releasing inventory (compensation)");
        Latency.simulate(200, 500);
        LOGGER.atInfo().addKeyValue("itemCount", items.size()).log("Inventory released");
    }
}
