package com.finflow.payment.infrastructure.kafka;

import com.finflow.events.fraud.FraudAnalysisRequestedEvent;
import com.finflow.events.payment.PaymentApprovedEvent;
import com.finflow.events.payment.PaymentRejectedEvent;
import com.finflow.payment.domain.payment.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private static final String FRAUD_ANALYSIS_REQUESTED = "fraud-analysis-requested";
    private static final String PAYMENT_APPROVED         = "payment-approved";
    private static final String PAYMENT_REJECTED         = "payment-rejected";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishFraudAnalysisRequested(Payment payment) {
        var event = new FraudAnalysisRequestedEvent(
                UUID.randomUUID(), Instant.now(),
                payment.getId(), payment.getPayerId(),
                payment.getAmount(), payment.getCurrency());

        kafkaTemplate.send(FRAUD_ANALYSIS_REQUESTED, payment.getId().toString(), event);
        log.info("published event={} paymentId={}", event.eventType(), payment.getId());
    }

    public void publishPaymentApproved(Payment payment) {
        var event = new PaymentApprovedEvent(
                UUID.randomUUID(), Instant.now(),
                payment.getId(), payment.getPayerId());

        kafkaTemplate.send(PAYMENT_APPROVED, payment.getId().toString(), event);
        log.info("published event={} paymentId={}", event.eventType(), payment.getId());
    }

    public void publishPaymentRejected(Payment payment) {
        var event = new PaymentRejectedEvent(
                UUID.randomUUID(), Instant.now(),
                payment.getId(), payment.getRejectionReason());

        kafkaTemplate.send(PAYMENT_REJECTED, payment.getId().toString(), event);
        log.info("published event={} paymentId={}", event.eventType(), payment.getId());
    }
}
