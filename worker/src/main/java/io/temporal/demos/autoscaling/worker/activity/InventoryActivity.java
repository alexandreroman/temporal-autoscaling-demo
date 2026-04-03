package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.demos.autoscaling.worker.model.Order;

import java.util.List;

/**
 * Manages inventory reservations for order items.
 * {@link #releaseInventory} serves as the Saga
 * compensation action for {@link #reserveInventory}.
 */
@ActivityInterface
public interface InventoryActivity {
    @ActivityMethod
    void reserveInventory(List<Order.Item> items);

    @ActivityMethod
    void releaseInventory(List<Order.Item> items);
}
