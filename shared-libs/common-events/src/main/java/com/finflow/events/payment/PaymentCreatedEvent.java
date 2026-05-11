package com.finflow.events.payment;

import com.finflow.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID paymentId,
        UUID payerId,
        UUID payeeId,
        BigDecimal amount,
        String currency
) implements DomainEvent {

    @Override
    public String eventType() {
        return "payment.created";
    }
}
