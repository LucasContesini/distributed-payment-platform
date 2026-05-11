# Architecture Overview

## System Context

FinFlow is a distributed payment platform composed of seven autonomous microservices communicating via HTTP (synchronous, low-latency flows) and Kafka (asynchronous, high-throughput flows). All inbound traffic enters through the API Gateway, which enforces authentication and rate limiting before routing to downstream services.

## Service Map

```
                          ┌─────────────────────────────────────────────────┐
                          │                  API Gateway :8080               │
                          │  JWT validation · Rate limiting · Request routing│
                          └──────┬──────────────────┬────────────────────────┘
                                 │ HTTP              │ HTTP
                    ┌────────────▼──────┐   ┌────────▼──────────┐
                    │  auth-service     │   │  payment-service  │
                    │  :8081            │   │  :8082            │
                    │  JWT · bcrypt     │   │  Saga orchestrator│
                    └───────────────────┘   └────────┬──────────┘
                                                     │ HTTP (sync)
                                            ┌────────▼──────────┐
                                            │  wallet-service   │
                                            │  :8083            │
                                            │  Balance · Lock   │
                                            └────────┬──────────┘
                                                     │
                              ┌──────────────────────▼──────────────────────┐
                              │                   Apache Kafka               │
                              │  payment-created · fraud-analysis-requested  │
                              │  fraud-analysis-completed · payment-approved  │
                              │  payment-rejected · notification-requested   │
                              └──────┬───────────────────────────────────────┘
                    ┌────────────────┤
         ┌──────────▼──────┐  ┌─────▼────────────┐  ┌──────────────────┐
         │  fraud-service  │  │notification-svc  │  │  audit-service   │
         │  :8084          │  │  :8085           │  │  :8086           │
         │  Risk analysis  │  │  Email · Push    │  │  Immutable log   │
         └─────────────────┘  └──────────────────┘  └──────────────────┘
```

## Data Isolation

Each service owns a dedicated PostgreSQL schema. Cross-schema queries are prohibited at the application level. The `payment-service` uses the Outbox pattern to guarantee atomic state + event publishing.

| Service | Schema | Notable Tables |
|---|---|---|
| auth-service | `auth` | `users`, `refresh_tokens` |
| payment-service | `payment` | `payments`, `outbox_events` |
| wallet-service | `wallet` | `wallets`, `wallet_entries` |
| fraud-service | `fraud` | `fraud_analyses` |
| notification-service | `notification` | `notification_logs` |
| audit-service | `audit` | `audit_events` |

## Payment Lifecycle

```
Client                   Gateway              payment-service       wallet-service
  │                         │                       │                    │
  │ POST /api/payments       │                       │                    │
  ├────────────────────────►│                       │                    │
  │                         │ JWT validated          │                    │
  │                         ├──────────────────────►│                    │
  │                         │                       │ Reserve balance    │
  │                         │                       ├───────────────────►│
  │                         │                       │◄───────────────────┤
  │                         │                       │                    │
  │                         │                       │──► [payment-created] ──► fraud-service (async)
  │                         │                       │                    │
  │◄───────────────────────────────────────── 202 Accepted
  │                         │                       │
  │                         │              fraud-analysis-completed
  │                         │                       │◄──────────────────── fraud-service
  │                         │                       │
  │                         │              payment-approved / payment-rejected
  │                         │                       ├──► wallet-service (settle/release)
  │                         │                       ├──► notification-service
  │                         │                       └──► audit-service
```

## Resiliency

- **Circuit Breaker:** wallet-service calls from payment-service are wrapped with Resilience4j circuit breakers (Phase 4)
- **Retry:** Kafka consumers retry 3 times with exponential backoff before publishing to DLQ
- **Idempotency:** All Kafka consumers check an `idempotency_key` before processing to handle at-least-once delivery
- **Pessimistic Locking:** `wallet-service` uses `SELECT ... FOR UPDATE` on balance rows to prevent race conditions

## Infrastructure

```
┌─────────────────────────────────────────────────────────────────┐
│  Local Development Stack (docker-compose.yml)                    │
│                                                                  │
│  PostgreSQL 16   Redis 7   Kafka 7.7   Kafka-UI                 │
│  Prometheus      Grafana   Zipkin                                │
└─────────────────────────────────────────────────────────────────┘
```
