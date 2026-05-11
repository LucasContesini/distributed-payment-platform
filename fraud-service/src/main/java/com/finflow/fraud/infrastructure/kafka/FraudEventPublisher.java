package com.finflow.fraud.infrastructure.kafka;

import com.finflow.events.fraud.FraudAnalysisCompletedEvent;
import com.finflow.fraud.domain.analysis.FraudDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class FraudEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(FraudEventPublisher.class);
    private static final String FRAUD_ANALYSIS_COMPLETED = "fraud-analysis-completed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FraudEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishFraudAnalysisCompleted(UUID paymentId, FraudDecision decision, String reason) {
        var mappedDecision = switch (decision) {
            case APPROVED -> FraudAnalysisCompletedEvent.FraudDecision.APPROVED;
            case REJECTED -> FraudAnalysisCompletedEvent.FraudDecision.REJECTED;
        };

        var event = new FraudAnalysisCompletedEvent(
                UUID.randomUUID(), Instant.now(), paymentId, mappedDecision, reason);

        kafkaTemplate.send(FRAUD_ANALYSIS_COMPLETED, paymentId.toString(), event);
        log.info("published event={} paymentId={} decision={}", event.eventType(), paymentId, decision);
    }
}
