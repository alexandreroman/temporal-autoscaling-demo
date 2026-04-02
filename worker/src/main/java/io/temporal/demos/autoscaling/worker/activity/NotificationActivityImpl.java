package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.demos.autoscaling.worker.model.Order;
import io.temporal.demos.autoscaling.worker.model.PaymentResult;
import io.temporal.demos.autoscaling.worker.model.ShipmentDetails;
import io.temporal.demos.autoscaling.worker.workflow.OrderWorkflow;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = OrderWorkflow.TASK_QUEUE)
class NotificationActivityImpl implements NotificationActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationActivityImpl.class);

    @Override
    public void sendConfirmation(Order order, PaymentResult payment, ShipmentDetails shipment) {
        Latency.simulate(100, 300);
        LOGGER.atInfo()
                .addKeyValue("orderId", order.orderId())
                .addKeyValue("customerId", order.customerId())
                .log("Confirmation sent");
    }
}
