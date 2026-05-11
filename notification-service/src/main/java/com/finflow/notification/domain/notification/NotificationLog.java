package com.finflow.notification.domain.notification;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_logs", schema = "notification")
public class NotificationLog {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID recipientId;

    @Column(nullable = false)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String channel;

    @Column(nullable = false)
    private Instant sentAt;

    protected NotificationLog() {}

    public static NotificationLog of(UUID recipientId, UUID paymentId, NotificationType type) {
        var log = new NotificationLog();
        log.id = UUID.randomUUID();
        log.recipientId = recipientId;
        log.paymentId = paymentId;
        log.type = type;
        log.channel = "EMAIL";
        log.sentAt = Instant.now();
        return log;
    }

    public UUID getId() { return id; }
    public UUID getRecipientId() { return recipientId; }
    public UUID getPaymentId() { return paymentId; }
    public NotificationType getType() { return type; }
    public String getChannel() { return channel; }
    public Instant getSentAt() { return sentAt; }
}
