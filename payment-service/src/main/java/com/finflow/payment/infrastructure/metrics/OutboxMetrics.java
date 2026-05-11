package com.finflow.payment.infrastructure.metrics;

import com.finflow.payment.infrastructure.persistence.SpringDataOutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OutboxMetrics {

    public OutboxMetrics(MeterRegistry registry, SpringDataOutboxEventRepository outboxRepo) {
        Gauge.builder("finflow.outbox.pending_events", outboxRepo,
                        repo -> repo.countByPublishedFalse())
                .description("Number of events waiting to be relayed to Kafka")
                .register(registry);
    }
}
