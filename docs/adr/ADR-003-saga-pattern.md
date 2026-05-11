# ADR-003: Choreography-based Saga for Distributed Transactions

**Date:** 2025-05-11  
**Status:** Accepted

## Context

A payment involves wallet reservation, fraud analysis, and final settlement — steps that span multiple services. Traditional 2PC is impractical given service autonomy and the async nature of fraud analysis.

## Decision

Implement the **Choreography Saga** pattern using Kafka events. Each service reacts to events from the previous step and publishes its own outcome.

### Payment Saga Flow

```
payment-service  → payment-created
wallet-service   → balance-reserved | insufficient-funds
payment-service  → fraud-analysis-requested (if reserved)
fraud-service    → fraud-analysis-completed (APPROVED | REJECTED)
payment-service  → payment-approved | payment-rejected
wallet-service   → balance-settled | balance-released
notification-service ← payment-approved | payment-rejected
audit-service    ← all events
```

### Compensation Transactions

| Failure Point | Compensation |
|---|---|
| Insufficient funds | Publish `payment-rejected` directly |
| Fraud rejection | Publish `balance-released` to wallet-service |
| Notification failure | Retry via DLQ; non-critical, no saga compensation |

## Consequences

**Positive:**
- No central orchestrator is a single point of failure
- Services remain loosely coupled
- Easy to add new participants (e.g., loyalty-service) without modifying existing services

**Negative:**
- Saga state is distributed — harder to query current status of a transaction
- Cyclic dependencies between services are possible if not carefully designed
- Compensation logic adds code complexity
