package com.finflow.audit.domain;

import java.util.List;
import java.util.UUID;

public interface AuditRecordRepository {
    AuditRecord save(AuditRecord record);
    List<AuditRecord> findByAggregateId(UUID aggregateId);
}
