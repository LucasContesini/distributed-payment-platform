package com.finflow.notification.infrastructure.kafka;

import com.finflow.events.payment.PaymentApprovedEvent;
import com.finflow.notification.application.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentApprovedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentApprovedConsumer.class);

    private final NotificationService notificationService;

    public PaymentApprovedConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "payment-approved",
            groupId = "notification-service",
            containerFactory = "paymentApprovedListenerFactory"
    )
    public void consume(ConsumerRecord<String, PaymentApprovedEvent> record) {
        var event = record.value();
        log.info("received event={} paymentId={}", event.eventType(), event.paymentId());
        notificationService.notifyPaymentApproved(event.payerId(), event.paymentId());
    }
}
