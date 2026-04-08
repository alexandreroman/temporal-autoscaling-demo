# Autoscaling

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
