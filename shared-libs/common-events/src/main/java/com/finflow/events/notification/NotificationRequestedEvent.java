package com.finflow.events.notification;

import com.finflow.domain.DomainEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationRequestedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID recipientId,
        NotificationType type,
        Map<String, String> payload
) implements DomainEvent {

    @Override
    public String eventType() {
        return "notification.requested";
    }

    public enum NotificationType { PAYMENT_APPROVED, PAYMENT_REJECTED, WELCOME }
}
