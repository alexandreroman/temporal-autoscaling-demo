# Temporal Autoscaling Demo

See [README.md](README.md) for architecture, setup, and
usage instructions.

## Rules

- All code, comments, and text must be in English only.
- Lines of code: max 120 columns. Text and prose
  (comments, documentation, CLAUDE.md): max 80 columns.
- ALWAYS use the `code-writer` agent for ANY code modification,
  no matter how small (including simple renames, find-and-replace,
  single-line edits, refactoring, and new code).
- ALWAYS use the `temporal` CLI to debug workflows and retrieve
  Temporal-related details (workflow state, history, search
  attributes, etc.) instead of guessing or relying on memory.
- NEVER use compound bash commands (`&&`, `;`). Use separate Bash
  tool calls instead.
- In Java, use `var` for local variable declarations whenever
  possible, and mark all local variables and fields `final`
  (except method arguments).
- In Java, `static final` constants must use
  `UPPER_SNAKE_CASE` naming.
- Use structured logging with the SLF4J 2.0 Fluent API:
  `LOGGER.atInfo().addKeyValue("k", v).log("message")`.
  Keep message strings clean — all data goes through
  `addKeyValue()`, never embedded in the message.
