# Temporal Autoscaling Demo

See [README.md](README.md) for architecture, setup,
and usage.

Project rules and conventions live in the
project-memory system at
`.claude/project-memory/MEMORY.md` — read it
before making changes.

## Agents

Both agents are provided by the
[skillbox](https://github.com/alexandreroman/skillbox)
plugin.

- Use the `code-writer` agent for ANY code
  modification, no matter how small (renames,
  find-and-replace, single-line edits,
  refactoring, new code). Never edit source
  files directly.
- Use the `code-reviewer` agent in read-only
  mode after non-trivial changes to audit for
  bugs, security issues, and spec violations.

## Reference

- [Metrics](docs/metrics.md)
- [Autoscaling](docs/autoscaling.md)
- [Load testing](docs/load-testing.md)
G
