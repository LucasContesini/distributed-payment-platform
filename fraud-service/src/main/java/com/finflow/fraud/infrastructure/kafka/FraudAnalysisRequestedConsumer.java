package com.finflow.fraud.infrastructure.kafka;

import com.finflow.events.fraud.FraudAnalysisRequestedEvent;
import com.finflow.fraud.application.FraudAnalysisService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FraudAnalysisRequestedConsumer {

    private static final Logger log = LoggerFactory.getLogger(FraudAnalysisRequestedConsumer.class);

    private final FraudAnalysisService fraudService;

    public FraudAnalysisRequestedConsumer(FraudAnalysisService fraudService) {
        this.fraudService = fraudService;
    }

    @KafkaListener(
            topics = "fraud-analysis-requested",
            groupId = "fraud-service",
            containerFactory = "fraudAnalysisRequestedListenerFactory"
    )
    public void consume(ConsumerRecord<String, FraudAnalysisRequestedEvent> record) {
        var event = record.value();
        log.info("received event={} paymentId={} amount={}", event.eventType(), event.paymentId(), event.amount());
        fraudService.analyze(event.paymentId(), event.payerId(), event.amount(), event.currency());
    }
}
