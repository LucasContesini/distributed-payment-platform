package com.finflow.events.fraud;

import com.finflow.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FraudAnalysisRequestedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID paymentId,
        UUID payerId,
        BigDecimal amount,
        String currency
) implements DomainEvent {

    @Override
    public String eventType() {
        return "fraud.analysis.requested";
    }
}
