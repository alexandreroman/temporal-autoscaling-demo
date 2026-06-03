# Project Memory

> When a new decision **contradicts** an existing
> memory note, do NOT silently override it.
> Instead: surface the conflict, quote the
> existing memory, explain how the new decision
> differs, and ask for explicit confirmation
> before updating. **Do NOT take any action** —
> no tool calls, no file writes — until confirmed.

- [General conventions](references/feedback_general_conventions.md) — English only, 120/80 line length
- [Kubernetes deploys via Taskfile](references/feedback_kubernetes_deploys.md) — task app-deploy/app-delete only, never kustomize or kubectl apply
- [Use temporal CLI for workflow debugging](references/feedback_temporal_debug.md) — fetch live state, never guess
- [Java conventions](references/feedback_java_conventions.md) — var, final, UPPER_SNAKE_CASE, SLF4J Fluent API
- [Worker Controller CRD version coupling](references/project_worker_controller_crd_coupling.md) — temporal-k8s controller bumps can break k8s/worker manifests (0.26.0 renamed kinds)
- [Cluster disk footprint and podman VM constraint](references/project_cluster_disk_footprint.md) — 37G VM is tight; demo isn't the culprit; clean host cruft, don't grow
- [Validating local app changes against the cluster](references/project_validate_local_changes_in_cluster.md) — app-deploy uses published images; validate un-pushed changes with the it profile on the host
