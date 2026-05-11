package com.finflow.events.payment;

import com.finflow.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record PaymentRejectedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID paymentId,
        String reason
) implements DomainEvent {

    @Override
    public String eventType() {
        return "payment.rejected";
    }
}
