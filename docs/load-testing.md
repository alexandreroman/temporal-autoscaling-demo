# Load Testing

Start `OrderWorkflow` instances by sending HTTP
requests to the console app. The console launches
workflows in batches with configurable delays,
which generates load and triggers autoscaling.

## Console URL

| Environment  | URL                                                  |
|--------------|------------------------------------------------------|
| Local/Docker | `http://localhost:8080`                              |
| Kubernetes   | `http://temporal-autoscaling-demo.127-0-0-1.nip.io` |

## Presets

| Preset   | Workflows | Batch size | Delay |
|----------|-----------|------------|-------|
| `normal` | 10        | 5          | 1 s   |
| `load`   | 1000      | 100        | 1 s   |

## Starting a scenario

Use `POST /scenarios` with form parameters
`totalCount`, `batchSize`, `delaySeconds`, and
`preset`.

Normal load (10 workflows):

```bash
curl -X POST http://localhost:8080/scenarios \
  -d 'totalCount=10&batchSize=5&delaySeconds=1&preset=normal'
```

Heavy load to trigger autoscaling (1000 workflows):

```bash
curl -X POST http://localhost:8080/scenarios \
  -d 'totalCount=1000&batchSize=100&delaySeconds=1&preset=load'
```

Custom scenario:

```bash
curl -X POST http://localhost:8080/scenarios \
  -d 'totalCount=500&batchSize=50&delaySeconds=2&preset=custom'
```

Replace `localhost:8080` with the Kubernetes URL
when running in a cluster.

When Grafana is configured, each scenario
automatically creates an annotation for
correlating load tests with system metrics.
