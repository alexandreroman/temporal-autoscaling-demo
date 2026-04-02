package io.temporal.demos.autoscaling.worker.activity;

import io.temporal.demos.autoscaling.worker.model.Order;
import io.temporal.demos.autoscaling.worker.model.ShipmentDetails;
import io.temporal.demos.autoscaling.worker.workflow.OrderWorkflow;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@ActivityImpl(taskQueues = OrderWorkflow.TASK_QUEUE)
class ShipmentActivityImpl implements ShipmentActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShipmentActivityImpl.class);
    private static final String[] CARRIERS = {"DHL", "FedEx", "UPS"};

    @Override
    public ShipmentDetails prepareShipment(Order order) {
        Latency.simulate(400, 1000);

        final var trackingNumber = "TRK-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
        final var carrier = CARRIERS[ThreadLocalRandom.current().nextInt(CARRIERS.length)];
        final var estimatedDelivery = LocalDate.now().plusDays(3 + ThreadLocalRandom.current().nextInt(5));

        LOGGER.atInfo()
                .addKeyValue("orderId", order.orderId())
                .addKeyValue("trackingNumber", trackingNumber)
                .addKeyValue("carrier", carrier)
                .addKeyValue("estimatedDelivery", estimatedDelivery)
                .log("Shipment prepared");

        return new ShipmentDetails(trackingNumber, carrier, estimatedDelivery);
    }
}
