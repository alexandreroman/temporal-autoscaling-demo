package io.temporal.demos.autoscaling.console.service;

import io.temporal.demos.autoscaling.console.model.Order;
import io.temporal.demos.autoscaling.console.model.PaymentRequest;
import io.temporal.demos.autoscaling.console.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generates randomized orders from a fixed product catalog.
 * Used to create realistic demo data for workflow scenarios.
 */
@Service
public class OrderFactory {

    private static final List<Product> CATALOG = List.of(
            new Product("SKU-001", "Laptop", new BigDecimal("999.99")),
            new Product("SKU-002", "Keyboard", new BigDecimal("79.99")),
            new Product("SKU-003", "Monitor", new BigDecimal("449.99")),
            new Product("SKU-004", "Mouse", new BigDecimal("39.99")),
            new Product("SKU-005", "Headset", new BigDecimal("129.99")),
            new Product("SKU-006", "Webcam", new BigDecimal("89.99")),
            new Product("SKU-007", "USB Hub", new BigDecimal("34.99")),
            new Product("SKU-008", "Desk Lamp", new BigDecimal("54.99")),
            new Product("SKU-009", "Chair", new BigDecimal("349.99")),
            new Product("SKU-010", "Standing Desk", new BigDecimal("599.99"))
    );

    public Order create() {
        final var random = ThreadLocalRandom.current();
        final var orderId = UUID.randomUUID().toString().substring(0, 8);
        final var customerId = String.format("%04d", random.nextInt(1_000, 10_000));

        final var itemCount = random.nextInt(1, 6);
        final var items = IntStream.range(0, itemCount)
                .mapToObj(_ -> {
                    final var product = CATALOG.get(random.nextInt(CATALOG.size()));
                    final var quantity = random.nextInt(1, 6);
                    return new Order.Item(
                            product.sku(), product.name(),
                            quantity, product.unitPrice()
                    );
                })
                .toList();

        final var totalAmount = items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var payment = new PaymentRequest("CARD", totalAmount, "EUR");
        return new Order(orderId, customerId, items, payment);
    }
}
