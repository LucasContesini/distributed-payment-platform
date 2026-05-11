package com.finflow.payment.domain.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", schema = "payment")
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant publishedAt;

    protected OutboxEvent() {}

    public static OutboxEvent of(UUID aggregateId, String eventType, String topic, String payload) {
        var event = new OutboxEvent();
        event.id = UUID.randomUUID();
        event.aggregateId = aggregateId;
        event.eventType = eventType;
        event.topic = topic;
        event.payload = payload;
        event.published = false;
        event.createdAt = Instant.now();
        return event;
    }

    public void markPublished() {
        this.published = true;
        this.publishedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getTopic() { return topic; }
    public String getPayload() { return payload; }
    public boolean isPublished() { return published; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
}
