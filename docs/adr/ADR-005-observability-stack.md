# ADR-005: Observability Stack

**Date:** 2025-05-11  
**Status:** Accepted

## Context

A distributed payment platform spans multiple processes, databases, and message queues. Debugging a failed transaction without distributed tracing means correlating logs manually across 5+ services. Detecting regressions in fraud rejection rate or payment approval latency requires metric aggregation. Structured logs are required for log aggregation pipelines (ELK, Loki).

## Decision

**Distributed Tracing:** Micrometer Tracing with the Brave bridge (`micrometer-tracing-bridge-brave`) and Zipkin exporter (`zipkin-reporter-brave`). Spring Boot 3.x auto-configures everything with a single BOM entry. Trace context is propagated via HTTP headers (W3C TraceContext) and Kafka message headers automatically.

**Metrics:** Micrometer + Prometheus pull model. Spring Boot Actuator exposes `/actuator/prometheus`. Custom business metrics:
- `finflow.payments.created/approved/rejected{reason}` — payment funnel
- `finflow.fraud.analyses{decision}` — fraud pipeline health
- `finflow.outbox.pending_events` — relay backlog indicator

**Structured Logging:** `logstash-logback-encoder` produces JSON output. `traceId` and `spanId` are injected from MDC automatically by Micrometer Tracing. Non-local profiles use JSON; local profile uses a human-readable pattern with `[traceId/spanId]`.

**Dashboards:** Grafana with 12 panels auto-provisioned at startup via the provisioning directory. Covers payment funnel, latency P95, fraud distribution, JVM heap, outbox backlog, and error rates.

## Consequences

**Positive:**
- Cross-service trace correlation: a single `traceId` links all log lines and spans for a payment, from API Gateway through to notification delivery
- Kafka message tracing: W3C trace context in Kafka headers means fraud-service spans are children of the payment-service span
- Outbox gauge provides early warning before relay backlog becomes a problem
- Grafana dashboard is ready on first `docker compose up`

**Negative:**
- 100% sampling (`probability=1.0`) is correct for development but must be reduced (e.g., 0.1) in production to avoid Zipkin storage overhead
- `logstash-logback-encoder` adds ~1MB to the runtime classpath (acceptable)
- Custom metrics require discipline to maintain — unused metrics add cardinality and cost
