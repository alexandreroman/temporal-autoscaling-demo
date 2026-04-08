# Load Testing

Start `OrderWorkflow` instances via the `temporal` CLI
to generate load and trigger autoscaling.

Workflow input JSON structure:

```json
{
  "orderId": "order-001",
  "customerId": "customer-42",
  "items": [
    {
      "sku": "SKU-1234",
      "label": "Wireless Mouse",
      "quantity": 2,
      "unitPrice": 29.99
    }
  ],
  "payment": {
    "method": "CreditCard",
    "amount": 59.98,
    "currency": "USD"
  }
}
```

Single workflow:

```bash
temporal workflow start \
  --address temporal.127-0-0-1.nip.io:7233 \
  --task-queue order-processing \
  --type OrderWorkflow \
  --workflow-id order-001 \
  --input '{"orderId":"order-001","customerId":"customer-42","items":[{"sku":"SKU-1234","label":"Wireless Mouse","quantity":2,"unitPrice":29.99}],"payment":{"method":"CreditCard","amount":59.98,"currency":"USD"}}'
```

Burst (500 workflows, parallel) to trigger autoscaling:

```bash
for i in $(seq 1 500); do
  temporal workflow start \
    --address temporal.127-0-0-1.nip.io:7233 \
    --task-queue order-processing \
    --type OrderWorkflow \
    --workflow-id "order-burst-$i" \
    --input "{\"orderId\":\"order-burst-$i\",\"customerId\":\"customer-42\",\"items\":[{\"sku\":\"SKU-1234\",\"label\":\"Wireless Mouse\",\"quantity\":1,\"unitPrice\":29.99}],\"payment\":{\"method\":\"CreditCard\",\"amount\":29.99,\"currency\":\"USD\"}}" &
done
wait
```
