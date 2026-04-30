---
name: "Java conventions: var, final, constants, logging"
description: "Java code style: var for locals, final everywhere, UPPER_SNAKE_CASE constants, SLF4J Fluent API"
type: feedback
---

# Java conventions: var, final, constants, logging

- Use `var` for local variable declarations
  whenever possible.
- Mark all local variables and fields `final`
  (except method arguments).
- `static final` constants must use
  `UPPER_SNAKE_CASE` naming.
- Use structured logging via the SLF4J 2.0
  Fluent API:
  `LOGGER.atInfo().addKeyValue("k", v).log("msg")`.
  Keep message strings clean — all data goes
  through `addKeyValue()`, never embedded in
  the message.

**Why:** immutability by default catches a
class of bugs at compile time; `var` reduces
visual noise; structured logs are queryable
and machine-parseable in observability tools.

**How to apply:** when writing or reviewing
Java, enforce these rules across new code.
Existing code may be brought into compliance
opportunistically when touched.
