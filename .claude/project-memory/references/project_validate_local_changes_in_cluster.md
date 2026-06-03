---
name: "Validating local app changes against the cluster"
description: "task app-deploy runs published ghcr.io:latest; validate un-pushed changes with the it Spring profile on the host"
type: project
---

# Validating local app changes against the cluster

`task app-deploy` deploys the **published**
`ghcr.io/.../...:latest` images. Its kbld step
resolves the `:latest` tag to the **remote**
registry digest, so it ignores any locally-built
or `kind load`-ed image. A local code/dependency
change is therefore NOT reflected in-cluster until
CI publishes a new image.

To validate **un-pushed** worker/console changes
against the live integration cluster, run the jars
on the host with the `it` Spring profile:

```
cd worker   && ./mvnw spring-boot:run -Dspring-boot.run.profiles=it
cd console  && ./mvnw spring-boot:run -Dspring-boot.run.profiles=it
```

They connect through the cluster ingress
(`temporal.127-0-0-1.nip.io:7233`, and
`otel`/`grafana`/`prometheus`.`127-0-0-1.nip.io`),
exercising real Temporal connectivity, the
`/actuator/health` probes, and OTLP metric export
(series land in Prometheus under
`service_name="autoscaling-demo-worker"`, e.g.
`order_duration_milliseconds`).

**Why:** it gives an honest runtime check of local
changes with **zero added cluster disk** — relevant
because the [[project_cluster_disk_footprint]]
37G VM sits ~90% full, so building + `kind load`-ing
two ~440MB images across the 4 Kind nodes is not
feasible and risks the shared cluster.

**How to apply:** for "update X then verify it works
in-cluster" tasks, build with Maven/Podman to prove
it compiles/packages, then use the host `it`-profile
run for runtime validation. Caveat: the host worker
registers as a **plain, unversioned** worker on
`order-processing` — the versioned / Worker
Controller config is gated behind
`on-cloud-platform: kubernetes`, so this path does
**not** exercise the worker-controller / HPA
autoscaling flow (that needs the published image
deployed in-cluster). See
[[project_worker_controller_crd_coupling]].
