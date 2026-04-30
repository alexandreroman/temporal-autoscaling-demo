---
name: "Use temporal CLI for workflow debugging"
description: "Always use the temporal CLI for workflow state, history, and search attributes"
type: feedback
---

# Use temporal CLI for workflow debugging

Always use the `temporal` CLI to debug
workflows and retrieve Temporal-related
details: workflow state, history, search
attributes, task queue stats, etc. Never
guess or rely on memory.

**Why:** Temporal state changes constantly
and is authoritative on the server. Guessing
leads to incorrect diagnoses and wasted
iterations.

**How to apply:** before reasoning about a
workflow's behavior, run `temporal workflow
describe`, `temporal workflow show`, or the
relevant subcommand to fetch live data.
