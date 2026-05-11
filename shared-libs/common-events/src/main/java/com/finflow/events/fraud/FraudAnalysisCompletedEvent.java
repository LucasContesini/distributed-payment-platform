package com.finflow.events.fraud;

import com.finflow.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record FraudAnalysisCompletedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID paymentId,
        FraudDecision decision,
        String reason
) implements DomainEvent {

    @Override
    public String eventType() {
        return "fraud.analysis.completed";
    }

    public enum FraudDecision { APPROVED, REJECTED }
}
