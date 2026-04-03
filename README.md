# Temporal Autoscaling Demo

Demonstrates how [Temporal](https://temporal.io) handles autoscaling
for workflows without data loss, thanks to **Durable Execution**.

Workers can be scaled up and down (or even restarted mid-flight)
while workflows keep running reliably. An order-processing workflow
is used as the example workload.

## Architecture

| Component | Stack | Purpose |
|-----------|-------|---------|
| **`worker/`** | Java 25, Spring Boot 4, Temporal SDK | Hosts the `OrderWorkflow` and its activities (payment, inventory, shipment, validation, notification) |
| **`console/`** | Java 25, Spring Boot 4, Thymeleaf | Web UI to trigger workflows with pre-defined load scenarios |

Both components expose metrics in **OpenTelemetry** format, visualized
through a Grafana dashboard.

## Prerequisites

- Java 25+
- [Temporal CLI](https://docs.temporal.io/cli) (`temporal`)
- Docker & Docker Compose (for containerized setup)

## Quick Start

### Local (bare-metal)

Start a local Temporal dev server, then run the worker and console
in separate terminals:

```bash
# Terminal 1
temporal server start-dev

# Terminal 2
cd worker && ./mvnw spring-boot:run

# Terminal 3
cd console && ./mvnw spring-boot:run
```

- **Console**: http://localhost:8080
- **Temporal UI**: http://localhost:8233

### Docker Compose

```bash
docker compose up --build
```

This starts Temporal, the worker, and the console. The console is
exposed on port **8080**.

## Kubernetes (Integration Environment)

The integration environment runs on a local Kubernetes cluster
provisioned by
[temporal-k8s](https://github.com/alexandreroman/temporal-k8s).
This project deploys Temporal alongside **Grafana** for metrics
visualization and **KEDA** for autoscaling workers based on
Temporal task-queue backlog.

Once the cluster is up, use the `it` Spring profile to connect:

```bash
# Terminal 1
cd worker && ./mvnw spring-boot:run -Dspring-boot.run.profiles=it

# Terminal 2
cd console && ./mvnw spring-boot:run -Dspring-boot.run.profiles=it
```

| Service | URL |
|---------|-----|
| Temporal UI | http://temporal.127-0-0-1.nip.io |
| Temporal API | `temporal.127-0-0-1.nip.io:7233` |
| OTel Collector | http://otel.127-0-0-1.nip.io:4318 |
| Grafana | http://grafana.127-0-0-1.nip.io |
| Prometheus | http://prometheus.127-0-0-1.nip.io |

### Kubernetes Deployment

Deploy and manage the application on Kubernetes using
[Task](https://taskfile.dev):

```bash
task app-deploy   # Deploy to Kubernetes
task app-delete   # Delete the deployment
```

`app-deploy` picks the best available toolchain: **kapp + kbld**,
**kapp** alone, or plain **kubectl**. Both tasks require
`kustomize`.

## Debugging

Inspect workflows via the Temporal CLI:

```bash
temporal workflow show   -w <workflow-id>
temporal workflow query  -w <workflow-id> --type <query-type>
temporal workflow signal -w <workflow-id> --name <signal-name>
temporal workflow stack  -w <workflow-id>
```

## License

[Apache License 2.0](LICENSE)
