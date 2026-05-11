package com.finflow.payment.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PaymentMetrics {

    private final MeterRegistry registry;
    private final Counter created;
    private final Counter approved;

    public PaymentMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.created = Counter.builder("finflow.payments.created")
                .description("Total payment requests received")
                .register(registry);
        this.approved = Counter.builder("finflow.payments.approved")
                .description("Total payments approved by fraud analysis")
                .register(registry);
    }

    public void recordCreated() {
        created.increment();
    }

    public void recordApproved() {
        approved.increment();
    }

    public void recordRejected(String reason) {
        Counter.builder("finflow.payments.rejected")
                .description("Total payments rejected")
                .tag("reason", reason)
                .register(registry)
                .increment();
    }
}
