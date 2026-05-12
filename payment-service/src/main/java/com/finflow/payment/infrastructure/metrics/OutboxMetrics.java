package com.finflow.payment.infrastructure.metrics;

import com.finflow.payment.domain.outbox.OutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OutboxMetrics {

    public OutboxMetrics(MeterRegistry registry, OutboxEventRepository outboxRepository) {
        Gauge.builder("finflow.outbox.pending_events", outboxRepository,
                        OutboxEventRepository::countUnpublished)
                .description("Number of events waiting to be relayed to Kafka")
                .register(registry);
    }
}
