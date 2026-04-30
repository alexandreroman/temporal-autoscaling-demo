---
name: "Kubernetes deploys via Taskfile"
description: "Use task app-deploy / app-delete; never run kustomize or kubectl apply directly"
type: feedback
---

# Kubernetes deploys via Taskfile

- Use `task app-deploy` to deploy the app to
  Kubernetes.
- Use `task app-delete` to remove it.
- Never run `kustomize build` or `kubectl
  apply` manually.

**Why:** the Taskfile encodes the correct
manifest pipeline (kustomize overlay
selection, version tagging, ordering). Running
the underlying tools directly bypasses that
logic and produces inconsistent state.

**How to apply:** route every K8s deploy or
teardown through the Taskfile targets. If a
target is missing, add it to `Taskfile.yml`
rather than running raw commands.
