package com.finflow.payment.infrastructure.kafka;

import com.finflow.events.fraud.FraudAnalysisCompletedEvent;
import com.finflow.payment.application.PaymentApplicationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FraudAnalysisCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(FraudAnalysisCompletedConsumer.class);

    private final PaymentApplicationService paymentService;

    public FraudAnalysisCompletedConsumer(PaymentApplicationService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(
            topics = "fraud-analysis-completed",
            groupId = "payment-service",
            containerFactory = "fraudAnalysisListenerFactory"
    )
    public void consume(ConsumerRecord<String, FraudAnalysisCompletedEvent> record) {
        var event = record.value();
        log.info("received event={} paymentId={} decision={}", event.eventType(), event.paymentId(), event.decision());
        paymentService.processFraudResult(event);
    }
}
