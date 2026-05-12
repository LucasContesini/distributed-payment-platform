# FinFlow — Distributed Payment Platform

A production-grade distributed payment platform simulating a real-world fintech backend. Built with Java 21, Spring Boot 3, Kafka, PostgreSQL, and Redis using microservices, event-driven architecture, and distributed systems patterns.

---

## Architecture

Seven autonomous services communicate over HTTP (synchronous flows) and Kafka (asynchronous pipelines). All external traffic enters through the API Gateway, which validates JWT tokens and enforces rate limits before routing downstream.

```
                    ┌──────────────────────────────────────────────────────┐
                    │                   API Gateway :8080                   │
                    │         JWT · Rate Limiting · Routing                 │
                    └─────────────────────┬────────────────────────────────┘
                                          │
            ┌─────────────────────────────┼──────────────────────────┐
            │                             │                          │
   ┌────────▼──────┐           ┌──────────▼──────────┐              │
   │ auth-service  │           │  payment-service     │              │
   │    :8081      │           │     :8082            │              │
   └───────────────┘           └──────────┬───────────┘              │
                                          │ HTTP                     │
                               ┌──────────▼───────────┐             │
                               │   wallet-service      │             │
                               │      :8083            │             │
                               └──────────┬────────────┘             │
                                          │                          │
                         ┌────────────────▼──────────────────────────▼─┐
                         │                  Apache Kafka                  │
                         └────────────┬────────────┬──────────────────────┘
                          ┌───────────▼──┐  ┌──────▼────────┐  ┌──────────────┐
                          │ fraud-service│  │notification   │  │audit-service │
                          │    :8084     │  │   :8085       │  │   :8086      │
                          └──────────────┘  └───────────────┘  └──────────────┘
```

### Implemented Patterns

| Pattern | Where |
|---|---|
| Saga (Choreography) | payment-service → Kafka → wallet, fraud, notification |
| Outbox Pattern | payment-service, wallet-service |
| Circuit Breaker | payment-service → wallet-service (Resilience4j) |
| Retry + DLQ | All Kafka consumers |
| Idempotency Keys | All Kafka consumers |
| JWT Authentication | api-gateway + auth-service |
| Rate Limiting | api-gateway (Redis) |
| Distributed Tracing | OpenTelemetry → Zipkin/Tempo |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4, Spring Cloud 2024 |
| Gateway | Spring Cloud Gateway (reactive) |
| Security | Spring Security 6, JJWT 0.12 |
| Messaging | Apache Kafka 7.7 |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Build | Gradle 8 (Kotlin DSL, version catalog) |
| Observability | OpenTelemetry, Prometheus, Grafana, Zipkin |
| Testing | JUnit 5, Mockito, Testcontainers |
| Infrastructure | Docker, Docker Compose |

---

## Services

| Service | Port | Description |
|---|---|---|
| api-gateway | 8080 | Ingress: JWT validation, rate limiting, routing |
| auth-service | 8081 | User registration, login, JWT + refresh token lifecycle |
| payment-service | 8082 | Transaction creation and saga orchestration |
| wallet-service | 8083 | Balance reservation, settlement, pessimistic locking |
| fraud-service | 8084 | Asynchronous risk analysis via Kafka |
| notification-service | 8085 | Email/push notification delivery |
| audit-service | 8086 | Immutable event audit log |

---

## Kafka Topics

| Topic | Partitions | Producers | Consumers |
|---|---|---|---|
| `payment-created` | 6 | payment-service | wallet-service, fraud-service |
| `payment-approved` | 6 | payment-service | notification-service, audit-service |
| `payment-rejected` | 6 | payment-service | notification-service, audit-service |
| `fraud-analysis-requested` | 6 | payment-service | fraud-service |
| `fraud-analysis-completed` | 6 | fraud-service | payment-service |
| `notification-requested` | 3 | payment-service | notification-service |
| `*.DLQ` | 1 | consumers (on failure) | ops alerting |

---

## Payment Flow

```
1.  Client authenticates       → POST /api/auth/login          → access_token
2.  Client creates payment     → POST /api/payments            → 202 Accepted
3.  payment-service validates  → publishes payment-created
4.  wallet-service reserves    → balance locked (pessimistic)
5.  payment-service publishes  → fraud-analysis-requested
6.  fraud-service analyzes     → publishes fraud-analysis-completed
7.  payment-service decides    → payment-approved | payment-rejected
8.  wallet-service settles     → balance settled | released
9.  notification-service       → user notified (async)
10. audit-service              → event stored immutably
```

---

## Quick Start

### Prerequisites

- Docker 24+
- Docker Compose v2

### Run the full stack

```bash
cd infra/docker
docker compose up -d
docker compose -f docker-compose.monitoring.yml up -d
```

### Register and authenticate

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "Password123!"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "Password123!"}'
```

### Monitoring

| Tool | URL | Credentials |
|---|---|---|
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | — |
| Zipkin | http://localhost:9411 | — |
| Kafka UI | http://localhost:8090 | — |

---

## Local Development (without Docker)

```bash
# Start infrastructure only
cd infra/docker
docker compose up -d postgres redis kafka zookeeper kafka-init

# Run auth-service
./gradlew :auth-service:bootRun

# Run api-gateway
./gradlew :api-gateway:bootRun
```

---

## Project Structure

```
distributed-payment-platform/
├── api-gateway/                    # Spring Cloud Gateway, JWT filter, rate limiting
├── auth-service/                   # JWT auth, refresh tokens, Flyway migrations
│   └── src/main/java/com/finflow/auth/
│       ├── domain/                 # Entities, repository interfaces, exceptions
│       ├── application/            # Use cases, commands, results
│       ├── infrastructure/         # JPA adapters, JWT impl, Spring Security
│       └── api/                    # REST controllers, DTOs, exception handler
├── payment-service/                # Phase 2
├── wallet-service/                 # Phase 2
├── fraud-service/                  # Phase 3
├── notification-service/           # Phase 3
├── audit-service/                  # Phase 3
├── shared-libs/
│   ├── common-domain/              # AggregateRoot, DomainEvent, base exceptions
│   └── common-events/              # Kafka event record definitions
├── infra/
│   ├── docker/                     # docker-compose.yml, init-db.sql
│   └── monitoring/                 # Prometheus, Grafana provisioning
└── docs/
    ├── adr/                        # Architecture Decision Records
    └── architecture/               # System overview, sequence diagrams
```

---

## Architecture Decisions

| # | Decision | Rationale |
|---|---|---|
| [ADR-001](docs/adr/ADR-001-microservices-architecture.md) | Microservices | Independent scaling, fault isolation, team autonomy |
| [ADR-002](docs/adr/ADR-002-kafka-event-driven-communication.md) | Kafka messaging | Durable, replayable, decoupled async communication |
| [ADR-003](docs/adr/ADR-003-saga-pattern.md) | Choreography Saga | No central orchestrator, services remain autonomous |
| [ADR-004](docs/adr/ADR-004-outbox-pattern.md) | Transactional Outbox | Atomic DB write + event publish, no dual-write risk |

---

## Scalability Considerations

- **Horizontal scaling:** All services are stateless. Scale by increasing replica count.
- **Kafka partitioning:** 6 partitions per high-volume topic supports up to 6 concurrent consumers per group.
- **Database connection pooling:** HikariCP configured per service; pool size tuned to prevent connection exhaustion.
- **Redis rate limiting:** Token bucket per API client, enforced at the gateway before downstream processing.

## Security Considerations

- JWT access tokens expire in 15 minutes; refresh tokens in 7 days.
- Refresh tokens are stored as SHA-256 hashes — raw tokens are never persisted.
- API Gateway removes all `X-User-*` headers from inbound requests before routing, preventing header injection.
- BCrypt with default cost factor 10 for password hashing.
- Schema-level isolation prevents cross-service data access at the database layer.

---

## Roadmap

- [x] Phase 1 — Infrastructure, API Gateway, Auth Service
- [x] Phase 2 — Payment Service, Wallet Service (synchronous flow)
- [x] Phase 3 — Kafka async pipeline: Fraud, Notification, Audit (fraud + notification)
- [x] Phase 4 — Transactional Outbox, Wallet Idempotency, ExponentialBackOff retry
- [x] Phase 5 — OpenTelemetry tracing, Micrometer custom metrics, structured JSON logging, Grafana dashboards
- [ ] Phase 6 — Documentation, Kubernetes manifests, CI/CD

---

## Status

🚧 Phase 5 complete — full observability stack: distributed tracing (Zipkin), custom Micrometer metrics, structured JSON logs (logstash-logback-encoder), and Grafana dashboard with 12 panels.
