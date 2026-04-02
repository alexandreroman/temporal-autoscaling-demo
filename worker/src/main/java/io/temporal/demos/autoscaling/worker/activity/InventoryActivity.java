package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.demos.autoscaling.worker.model.Order;

import java.util.List;

@ActivityInterface
public interface InventoryActivity {
    @ActivityMethod
    void reserveInventory(List<Order.Item> items);

    @ActivityMethod
    void releaseInventory(List<Order.Item> items);
}
