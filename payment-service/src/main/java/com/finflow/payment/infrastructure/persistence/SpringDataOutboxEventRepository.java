package com.finflow.payment.infrastructure.persistence;

import com.finflow.payment.domain.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface SpringDataOutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    long countByPublishedFalse();

    @Query(value = """
            SELECT * FROM payment.outbox_events
            WHERE published = false
            ORDER BY created_at ASC
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> findUnpublishedWithLock(@Param("limit") int limit);
}
