# Metrics

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
