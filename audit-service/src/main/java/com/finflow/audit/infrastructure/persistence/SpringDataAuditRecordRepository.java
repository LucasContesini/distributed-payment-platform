package com.finflow.audit.infrastructure.persistence;

import com.finflow.audit.domain.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataAuditRecordRepository extends JpaRepository<AuditRecord, UUID> {
    List<AuditRecord> findByAggregateIdOrderByOccurredAtAsc(UUID aggregateId);
}
