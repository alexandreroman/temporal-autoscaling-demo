---
name: "Worker Controller CRD version coupling"
description: "Demo k8s manifests track the Worker Controller CRD kinds shipped by temporal-k8s; bumps can break them"
type: project
---

# Worker Controller CRD version coupling

The `k8s/worker/` manifests depend on the exact CRD
API version of the Temporal Worker Controller deployed
by the neighbouring `temporal-k8s` repo. Worker
Controller **0.26.0** renamed and **hard-rejected** the
legacy kinds at creation time:

- `TemporalWorkerDeployment` → `WorkerDeployment`
- `TemporalConnection` → `Connection`
- `WorkerResourceTemplate.spec.temporalWorkerDeploymentRef`
  → `workerDeploymentRef`

The old kinds still exist as CRDs but the API server
returns `Invalid: ... is deprecated and cannot be
created` — so a stale manifest fails `task app-deploy`
hard, it does not just warn.

**Why:** the demo and the platform live in two separate
repos with no version pin between them; the platform can
bump the controller (and its CRDs) independently.

**How to apply:** whenever `temporal-k8s` bumps the
Temporal Worker Controller, re-validate `task app-deploy`
and, if it fails, diff the live CRD schema
(`kubectl explain workerdeployment.spec`) against
`k8s/worker/*.yaml`. Field structures were identical
across the 0.26.0 rename — only kinds/ref names changed.
