package com.finflow.audit.infrastructure.kafka;

import com.finflow.audit.application.AuditService;
import com.finflow.audit.domain.AuditEventType;
import com.finflow.events.payment.PaymentApprovedEvent;
import com.finflow.events.payment.PaymentCreatedEvent;
import com.finflow.events.payment.PaymentRejectedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditEventConsumer.class);

    private final AuditService auditService;

    public AuditEventConsumer(AuditService auditService) {
        this.auditService = auditService;
    }

    @KafkaListener(topics = "payment-created", groupId = "audit-service", containerFactory = "paymentCreatedListenerFactory")
    public void onPaymentCreated(ConsumerRecord<String, PaymentCreatedEvent> record) {
        var event = record.value();
        log.info("audit received event={} paymentId={}", event.eventType(), event.paymentId());
        auditService.record(event.paymentId(), AuditEventType.PAYMENT_CREATED, event, "payment-service", event.occurredAt());
    }

    @KafkaListener(topics = "payment-approved", groupId = "audit-service", containerFactory = "paymentApprovedListenerFactory")
    public void onPaymentApproved(ConsumerRecord<String, PaymentApprovedEvent> record) {
        var event = record.value();
        log.info("audit received event={} paymentId={}", event.eventType(), event.paymentId());
        auditService.record(event.paymentId(), AuditEventType.PAYMENT_APPROVED, event, "payment-service", event.occurredAt());
    }

    @KafkaListener(topics = "payment-rejected", groupId = "audit-service", containerFactory = "paymentRejectedListenerFactory")
    public void onPaymentRejected(ConsumerRecord<String, PaymentRejectedEvent> record) {
        var event = record.value();
        log.info("audit received event={} paymentId={}", event.eventType(), event.paymentId());
        auditService.record(event.paymentId(), AuditEventType.PAYMENT_REJECTED, event, "payment-service", event.occurredAt());
    }
}
