package com.finflow.audit.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_records", schema = "audit")
public class AuditRecord {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditEventType eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private String sourceService;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private Instant recordedAt;

    protected AuditRecord() {}

    public static AuditRecord of(UUID aggregateId, AuditEventType eventType, String payload, String sourceService, Instant occurredAt) {
        var record = new AuditRecord();
        record.id = UUID.randomUUID();
        record.aggregateId = aggregateId;
        record.eventType = eventType;
        record.payload = payload;
        record.sourceService = sourceService;
        record.occurredAt = occurredAt;
        record.recordedAt = Instant.now();
        return record;
    }

    public UUID getId() { return id; }
    public UUID getAggregateId() { return aggregateId; }
    public AuditEventType getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public String getSourceService() { return sourceService; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getRecordedAt() { return recordedAt; }
}
