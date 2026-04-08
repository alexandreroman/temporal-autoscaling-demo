# Autoscaling

HPA scaling signal:
`temporal_task_queue_backlog` — a Prometheus recording
rule that sums `approximate_backlog_count` across all
partitions and versions. This global metric is
responsive even with short-lived workflows, making it
well-suited for demo scenarios.

A per-version metric
(`temporal_worker_slots_used_by_version`) is also
available as an external metric for production use
with longer-running workloads.

Pipeline (backlog): Temporal Server (Prometheus scrape)
→ PodMonitor → Prometheus → recording rule →
prometheus-adapter (external metric) → HPA.

Pipeline (slots): Worker SDK (Micrometer) → OTLP →
OTel Collector → Prometheus remote write → recording
rule → prometheus-adapter (external metric).

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
  template with `temporal_task_queue_backlog`
- `worker/src/main/resources/application.yaml` —
  version tags (K8s profile, second YAML document)
