# ADR-001: Microservices Architecture

**Date:** 2025-05-11  
**Status:** Accepted

## Context

FinFlow must handle high transaction volumes, support independent team ownership, and allow selective scaling of components under load. A monolith would couple the fraud pipeline (CPU-intensive, async) with the payment processing flow (latency-sensitive, sync), making SLA management impractical.

## Decision

Adopt a microservices architecture with seven autonomous services:

| Service | Responsibility | Protocol |
|---|---|---|
| api-gateway | Ingress, JWT validation, rate limiting | HTTP |
| auth-service | Identity, token lifecycle | HTTP |
| payment-service | Transaction orchestration | HTTP + Kafka |
| wallet-service | Balance reservation and settlement | HTTP + Kafka |
| fraud-service | Asynchronous risk analysis | Kafka |
| notification-service | User notifications | Kafka |
| audit-service | Immutable event log | Kafka |

Each service owns its database schema (`payment`, `wallet`, etc.) inside a shared PostgreSQL instance. Schema isolation is enforced at the DDL level; no cross-schema JOINs are permitted.

## Consequences

**Positive:**
- Independent deployability and scaling
- Technology heterogeneity at service boundary
- Fault isolation: fraud-service degradation does not block payment processing

**Negative:**
- Distributed system complexity: distributed tracing, eventual consistency, network failures
- Operational overhead: more processes to monitor and deploy
- Testing surface increases: integration tests require Testcontainers or a local compose stack
