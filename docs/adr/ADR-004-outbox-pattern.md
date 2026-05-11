# ADR-004: Transactional Outbox for Reliable Event Publishing

**Date:** 2025-05-11  
**Status:** Accepted

## Context

Publishing a Kafka event in the same logical unit as a database write creates a dual-write problem. If the service crashes after the DB commit but before the Kafka publish, the event is lost. This violates the saga's consistency guarantees.

## Decision

Implement the **Transactional Outbox** pattern in `payment-service` and `wallet-service`.

### Mechanism

1. Within the same database transaction:
   - Write the aggregate state change
   - Insert a row into `<schema>.outbox_events` with the serialized event payload

2. A dedicated **relay process** (scheduled every 500ms) polls for unpublished outbox rows, publishes them to Kafka, and marks them as `published`.

3. The relay uses `SELECT ... FOR UPDATE SKIP LOCKED` to support multiple relay instances without contention.

### Outbox Schema

```sql
CREATE TABLE payment.outbox_events (
    id            UUID PRIMARY KEY,
    aggregate_id  UUID         NOT NULL,
    event_type    VARCHAR(100) NOT NULL,
    payload       JSONB        NOT NULL,
    published     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL,
    published_at  TIMESTAMPTZ
);
CREATE INDEX idx_outbox_unpublished ON payment.outbox_events (published, created_at)
    WHERE published = FALSE;
```

## Consequences

**Positive:**
- Event publishing is atomic with the state change — no lost events
- Resilient to Kafka unavailability: events accumulate in the outbox and are published when Kafka recovers

**Negative:**
- At-least-once delivery: consumers must implement idempotency checks (`idempotency_key` column)
- Polling introduces latency (typically < 1s), which is acceptable for this use case
- Outbox table must be monitored for accumulation (metric: `outbox_pending_events_total`)
