# ADR-002: Kafka for Asynchronous Communication

**Date:** 2025-05-11  
**Status:** Accepted

## Context

Fraud analysis is inherently asynchronous and should not block the payment confirmation response. Notifications and audit logging are fire-and-forget. These use cases require durable, ordered, replay-capable messaging.

## Decision

Use Apache Kafka as the event backbone for all asynchronous service communication. Each domain event has a dedicated topic. Topic partition count is sized to support parallel consumption per consumer group.

### Topic Configuration

| Topic | Partitions | Consumers |
|---|---|---|
| `payment-created` | 6 | wallet-service, fraud-service |
| `payment-approved` | 6 | notification-service, audit-service |
| `payment-rejected` | 6 | notification-service, audit-service |
| `fraud-analysis-requested` | 6 | fraud-service |
| `fraud-analysis-completed` | 6 | payment-service |
| `notification-requested` | 3 | notification-service |
| `*.DLQ` | 1 | ops alerting |

### DLQ Strategy

Consumers that fail after 3 retries (exponential backoff: 1s, 2s, 4s) publish the original message to the corresponding `.DLQ` topic with metadata headers (`X-Error-Reason`, `X-Retry-Count`, `X-Original-Offset`).

## Consequences

**Positive:**
- Decoupled services: producers are unaware of consumers
- Event replay for audit, debugging, and new service onboarding
- Natural backpressure through Kafka consumer lag

**Negative:**
- At-least-once delivery requires idempotent consumers
- Message ordering is guaranteed only within a partition — partition keys must be chosen carefully (use `paymentId` for payment events)
- Additional operational complexity: Kafka, Zookeeper, consumer lag monitoring
