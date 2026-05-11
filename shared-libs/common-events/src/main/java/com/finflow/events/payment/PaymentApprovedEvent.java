package com.finflow.events.payment;

import com.finflow.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record PaymentApprovedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID paymentId,
        UUID payerId
) implements DomainEvent {

    @Override
    public String eventType() {
        return "payment.approved";
    }
}
