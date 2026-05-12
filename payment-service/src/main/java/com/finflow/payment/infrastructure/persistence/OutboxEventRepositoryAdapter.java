package com.finflow.payment.infrastructure.persistence;

import com.finflow.payment.domain.outbox.OutboxEvent;
import com.finflow.payment.domain.outbox.OutboxEventRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    private final SpringDataOutboxEventRepository delegate;

    OutboxEventRepositoryAdapter(SpringDataOutboxEventRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return delegate.save(event);
    }

    @Override
    public List<OutboxEvent> findUnpublishedWithLock(int limit) {
        return delegate.findUnpublishedWithLock(limit);
    }

    @Override
    public long countUnpublished() {
        return delegate.countByPublishedFalse();
    }
}
