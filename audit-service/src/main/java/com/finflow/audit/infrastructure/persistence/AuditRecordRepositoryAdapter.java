package com.finflow.audit.infrastructure.persistence;

import com.finflow.audit.domain.AuditRecord;
import com.finflow.audit.domain.AuditRecordRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
class AuditRecordRepositoryAdapter implements AuditRecordRepository {

    private final SpringDataAuditRecordRepository delegate;

    AuditRecordRepositoryAdapter(SpringDataAuditRecordRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public AuditRecord save(AuditRecord record) {
        return delegate.save(record);
    }

    @Override
    public List<AuditRecord> findByAggregateId(UUID aggregateId) {
        return delegate.findByAggregateIdOrderByOccurredAtAsc(aggregateId);
    }
}
