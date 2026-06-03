---
name: "Cluster disk footprint and podman VM constraint"
description: "Full demo validation needs ~33G; the 37G podman VM is tight; the demo itself is not the culprit"
type: project
---

# Cluster disk footprint and podman VM constraint

End-to-end validation (cluster + full platform + demo +
a heavy load that scales workers 1→5) drives the shared
podman machine VM to ~33G used. The VM is provisioned at
only **37 GB** (abnormally small; podman default is
~100 GB) and is **shared with other projects**, so it
hit 100% during the first run — which cascaded into
`ImagePullBackOff` on new worker pods and a
`CreateContainerError` on `temporal-postgresql`.

Where the space goes (NOT the demo's fault):
- Kind node containerd stores: ~10-12G, because the
  `temporal-k8s` Kind cluster has **3 worker nodes** and
  each replicates the platform images (Temporal,
  full kube-prometheus-stack, CNPG, cert-manager,
  Traefik, OTel).
- Host cruft: systemd journal + transient buildah temp
  in `/var/tmp` (~5G, reclaimed once via
  `journalctl --vacuum-size`).
- The demo repo only adds the worker/console images
  (~500MB each).

**Decision (2026-06-03):** do NOT grow the VM disk and
do NOT trim the Kind node count; just reclaim host cruft.
Rationale: images are cached on the nodes after the first
run, so re-running the load (re-scale 1→5) pulls nothing
new and the cluster stays usable at ~90% full.

**How to apply:** if image pulls fail with
`no space left on device`, the fix is host-VM space, not
the demo manifests. `podman image prune` reclaims little
(layers shared with in-use images). Durable options if it
recurs: `podman machine set --disk-size 100` (restarts the
VM/cluster) or reduce `temporal-k8s` to 1-2 worker nodes
(weakens the topology-spread story).
