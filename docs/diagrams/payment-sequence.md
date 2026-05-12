# Payment Lifecycle — Sequence Diagram

## Happy Path (Fraud Approved)

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant GW as api-gateway
    participant PS as payment-service
    participant WS as wallet-service
    participant Outbox as outbox_events (DB)
    participant Relay as OutboxRelayService
    participant K as Kafka
    participant FS as fraud-service
    participant NS as notification-service

    Client->>GW: POST /api/payments {payeeId, amount, idempotencyKey}
    GW->>GW: Validate JWT → extract X-User-Id
    GW->>PS: POST /payments {payerId=X-User-Id, ...}

    PS->>PS: Idempotency check (idempotencyKey)
    PS->>PS: Create Payment{PENDING}

    PS->>WS: POST /internal/wallets/reserve {paymentId, userId, amount}
    WS->>WS: SELECT wallet FOR UPDATE (pessimistic lock)
    WS->>WS: wallet.reserve(amount) → reservedBalance += amount
    WS->>WS: INSERT wallet_entries{RESERVATION}
    WS-->>PS: 204 No Content

    PS->>PS: payment.markPendingFraudReview()
    PS->>Outbox: INSERT outbox_events{fraud-analysis-requested, published=false}
    PS-->>GW: 202 Accepted {id, status=PENDING_FRAUD_REVIEW}
    GW-->>Client: 202 Accepted

    Note over Relay: @Scheduled every 500ms
    Relay->>Outbox: SELECT * FOR UPDATE SKIP LOCKED WHERE published=false
    Relay->>K: PRODUCE fraud-analysis-requested {paymentId, amount}
    Relay->>Outbox: UPDATE published=true

    K->>FS: CONSUME fraud-analysis-requested
    FS->>FS: Idempotency check (paymentId)
    FS->>FS: evaluate(amount) → APPROVED
    FS->>FS: INSERT fraud_analyses{APPROVED}
    FS->>K: PRODUCE fraud-analysis-completed{APPROVED}

    K->>PS: CONSUME fraud-analysis-completed{APPROVED}
    PS->>WS: POST /internal/wallets/settle {paymentId, userId, amount}
    WS->>WS: SELECT wallet FOR UPDATE
    WS->>WS: wallet.settle(amount) → balance -= amount, reservedBalance -= amount
    WS->>WS: INSERT wallet_entries{SETTLEMENT}
    WS-->>PS: 204 No Content

    PS->>PS: payment.approve()
    PS->>Outbox: INSERT outbox_events{payment-approved}
    Relay->>K: PRODUCE payment-approved

    K->>NS: CONSUME payment-approved
    NS->>NS: INSERT notification_logs{PAYMENT_APPROVED}
    NS->>NS: log "Sending EMAIL notification"
```

## Rejection Path (Insufficient Funds)

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant GW as api-gateway
    participant PS as payment-service
    participant WS as wallet-service
    participant K as Kafka
    participant NS as notification-service

    Client->>GW: POST /api/payments {amount=999999}
    GW->>PS: POST /payments
    PS->>WS: POST /internal/wallets/reserve
    WS->>WS: availableBalance < amount → throw InsufficientFundsException
    WS-->>PS: 422 Unprocessable Entity
    PS->>PS: WalletServiceClient maps 422 → InsufficientFundsException
    PS->>PS: payment.reject("Insufficient funds")
    PS->>PS: enqueue payment-rejected event to outbox
    PS-->>GW: 202 Accepted {status=REJECTED, reason=Insufficient funds}
    GW-->>Client: 202 Accepted

    Note over PS: Outbox relay publishes payment-rejected
    PS->>K: PRODUCE payment-rejected
    K->>NS: CONSUME payment-rejected → INSERT notification_logs{PAYMENT_REJECTED}
```
