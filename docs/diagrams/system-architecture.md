# System Architecture

## Service Topology

```mermaid
graph TB
    Client([Client / Browser])

    subgraph Ingress
        GW[API Gateway\n:8080\nJWT · Rate Limit · Routing]
    end

    subgraph Auth Domain
        AS[auth-service\n:8081\nJWT · bcrypt · Flyway]
        AUTHDB[(auth schema\nusers\nrefresh_tokens)]
    end

    subgraph Payment Domain
        PS[payment-service\n:8082\nOrchestrator · Outbox]
        PDB[(payment schema\npayments\noutbox_events)]
    end

    subgraph Wallet Domain
        WS[wallet-service\n:8083\nBalance · Locking]
        WDB[(wallet schema\nwallets\nwallet_entries)]
    end

    subgraph Async Pipeline
        KF[(Apache Kafka\n6 topics + DLQs)]
        FS[fraud-service\n:8084\nRisk Analysis]
        NS[notification-service\n:8085\nEmail · Push]
        FDB[(fraud schema\nfraud_analyses)]
        NDB[(notification schema\nnotification_logs)]
    end

    subgraph Infrastructure
        RD[(Redis\nRate Limiting)]
        PR[Prometheus]
        GF[Grafana\n12-panel dashboard]
        ZP[Zipkin\nDistributed Tracing]
    end

    Client -->|HTTPS| GW
    GW -->|POST /auth/*| AS
    GW -->|POST /payments| PS
    GW -->|GET /wallets| WS

    AS --- AUTHDB
    PS --- PDB
    WS --- WDB

    PS -->|HTTP reserve / settle / release| WS
    PS -->|Outbox Relay → fraud-analysis-requested| KF
    KF -->|fraud-analysis-requested| FS
    FS -->|fraud-analysis-completed| KF
    KF -->|fraud-analysis-completed| PS
    PS -->|payment-approved / payment-rejected| KF
    KF -->|payment-approved| NS
    KF -->|payment-rejected| NS

    FS --- FDB
    NS --- NDB

    GW --- RD
    PS & WS & FS & NS & AS -->|/actuator/prometheus| PR
    PR --> GF
    PS & WS & FS & NS & AS -->|spans| ZP
```

## Data Isolation

Each service owns exactly one PostgreSQL schema. Cross-schema queries are prohibited at the application level — each `application.yml` sets `hibernate.default_schema` and Flyway `schemas` to a single named schema.

## Communication Matrix

| From | To | Protocol | When |
|---|---|---|---|
| api-gateway | auth-service | HTTP (sync) | Authentication |
| api-gateway | payment-service | HTTP (sync) | Payment creation |
| api-gateway | wallet-service | HTTP (sync) | Balance queries |
| payment-service | wallet-service | HTTP (sync) | reserve · settle · release |
| payment-service | Kafka | async (outbox) | After wallet reservation |
| fraud-service | Kafka | async | Analysis result |
| notification-service | Kafka | async (consume) | Payment outcome |
