# Temporal Autoscaling Demo

See [README.md](README.md) for architecture, setup, and
usage instructions.

## Rules

- All code, comments, and text must be in English only.
- Lines of code: max 120 columns. Text and prose
  (comments, documentation, CLAUDE.md): max 80 columns.
- ALWAYS use the `code-writer` agent for ANY code modification,
  no matter how small (including simple renames, find-and-replace,
  single-line edits, refactoring, and new code).
- ALWAYS use the `temporal` CLI to debug workflows and retrieve
  Temporal-related details (workflow state, history, search
  attributes, etc.) instead of guessing or relying on memory.
- NEVER use compound bash commands (`&&`, `;`). Use separate Bash
  tool calls instead.
- In Java, use `var` for local variable declarations whenever
  possible, and mark all local variables and fields `final`
  (except method arguments).
- In Java, `static final` constants must use
  `UPPER_SNAKE_CASE` naming.
- Use structured logging with the SLF4J 2.0 Fluent API:
  `LOGGER.atInfo().addKeyValue("k", v).log("message")`.
  Keep message strings clean — all data goes through
  `addKeyValue()`, never embedded in the message.
- Use `task app-deploy` / `task app-delete`
  (defined in Taskfile.yml) to deploy or remove the
  app in Kubernetes. Never run kustomize or kubectl
  apply manually.

## Metrics

Custom Micrometer metrics exposed by the worker:

- `order.status` — Counter, tag `status` (UpperCamelCase).
  Incremented at each order status transition.
- `order.duration` — Timer. End-to-end workflow duration
  (both success and failure paths).
- `order.activity.duration` — Timer, tag `activity`
  (Validation, Inventory, Payment, Shipment,
  Notification). Execution time of each activity.
- `order.failure` — Counter, tag `errorType`
  (e.g. InsufficientFundsError, GatewayTimeoutError).
  Incremented on workflow failure.
- `order.compensation` — Counter. Incremented when a Saga
  compensation is triggered.

Metrics are recorded via SDK interceptors
(`OrderMetricsWorkerInterceptor`): workflow duration,
failures, and compensations are captured in a
`WorkflowInboundCallsInterceptor`; activity durations
are captured in an `ActivityInboundCallsInterceptor`.
Status transitions are emitted directly in the workflow
via `Workflow.getMetricsScope()`.

Actuator endpoint:
`GET /actuator/metrics/{name}` on management port (9081).

Temporal SDK metrics are also available via the
`MicrometerClientStatsReporter` bridge
(prefixed `temporal_`).

## Autoscaling

HPA scaling signal:
`temporal_worker_slots_used_by_version` — a Prometheus
recording rule that sums `temporal_worker_task_slots_used`
per version (grouped by `temporal_worker_deployment_name`,
`temporal_worker_build_id`, `temporal_namespace`).

Pipeline: Worker SDK (Micrometer) → OTLP → OTel
Collector → Prometheus remote write → recording rule →
prometheus-adapter (external metric) → HPA.

Version-identifying tags are added as Micrometer common
tags in the K8s profile of `application.yaml`:
- `temporal.worker.deployment.name`
- `temporal.worker.build.id`
- `temporal.namespace`

These translate to underscore-separated Prometheus
labels via `UnderscoreEscapingWithSuffixes`, matching
the labels the Worker Controller auto-injects into
each versioned HPA's `matchLabels`.

Key files:
- `k8s/worker/worker-resource-template.yaml` — HPA
  template with `temporal_worker_slots_used_by_version`
- `worker/src/main/resources/application.yaml` —
  version tags (K8s profile, second YAML document)

## Load Testing

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
