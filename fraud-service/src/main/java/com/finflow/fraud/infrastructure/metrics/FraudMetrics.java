package com.finflow.fraud.infrastructure.metrics;

import com.finflow.fraud.domain.analysis.FraudDecision;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class FraudMetrics {

    private final MeterRegistry registry;

    public FraudMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordAnalysis(FraudDecision decision) {
        Counter.builder("finflow.fraud.analyses")
                .description("Total fraud analyses performed")
                .tag("decision", decision.name())
                .register(registry)
                .increment();
    }
}
