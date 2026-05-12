package com.finflow.audit.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.audit.domain.AuditEventType;
import com.finflow.audit.domain.AuditRecord;
import com.finflow.audit.domain.AuditRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditRecordRepository repository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditRecordRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void record(UUID aggregateId, AuditEventType eventType, Object payload, String sourceService, Instant occurredAt) {
        try {
            var serialized = objectMapper.writeValueAsString(payload);
            var record = AuditRecord.of(aggregateId, eventType, serialized, sourceService, occurredAt);
            repository.save(record);
            log.info("audit aggregateId={} eventType={} source={}", aggregateId, eventType, sourceService);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit payload for aggregateId={} eventType={}", aggregateId, eventType, e);
        }
    }

    @Transactional(readOnly = true)
    public java.util.List<AuditRecord> findByAggregateId(UUID aggregateId) {
        return repository.findByAggregateId(aggregateId);
    }
}
