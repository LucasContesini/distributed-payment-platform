package com.finflow.payment.domain.outbox;

import java.util.List;

public interface OutboxEventRepository {
    OutboxEvent save(OutboxEvent event);
    List<OutboxEvent> findUnpublishedWithLock(int limit);
    long countUnpublished();
}
