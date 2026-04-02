# Temporal Autoscaling demo

## Architecture

This project demonstrates Temporal's ability to handle autoscaling
for workflows without data loss, thanks to Durable Execution.

### Components

- **`worker/`** — Temporal Worker (Java 25 / Spring Boot 4). Hosts
  workflow and activity implementations. Uses
  `temporal-spring-boot-starter` for auto-registration and
  `spring-boot-starter-actuator` for health/metrics endpoints.
- **`console/`** — Web Console (Java 25 / Spring Boot 4 /
  Thymeleaf). A lightweight frontend used solely to trigger
  workflows with pre-defined load scenarios. Uses
  `spring-boot-starter-webmvc` + Thymeleaf for server-side
  rendering and the Temporal SDK to start workflows.

### Observability

Both components expose metrics in OpenTelemetry format, consumed
by a Grafana dashboard to visualize workflow throughput, latency,
and autoscaling behavior.

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

## Local Development

```bash
temporal server start-dev

cd worker && ./mvnw spring-boot:run        # Terminal 2
cd console && ./mvnw spring-boot:run       # Terminal 3
```

Temporal UI: http://localhost:8233

## Integration

Spring applications must use the `it` profile to connect
to the integration environment:

```bash
cd worker && ./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=it              # Terminal 2
cd console && ./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=it              # Terminal 3
```

Temporal UI: http://temporal.127-0-0-1.nip.io
Temporal API: temporal.127-0-0-1.nip.io:7233
OTel Collector: http://otel.127-0-0-1.nip.io:4318
Grafana: http://grafana.127-0-0-1.nip.io
Prometheus: http://prometheus.127-0-0-1.nip.io

## Debugging

- `temporal workflow show|query|signal|stack` to inspect workflows
  via CLI.
