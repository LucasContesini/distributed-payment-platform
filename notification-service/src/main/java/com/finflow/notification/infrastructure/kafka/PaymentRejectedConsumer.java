package com.finflow.notification.infrastructure.kafka;

import com.finflow.events.payment.PaymentRejectedEvent;
import com.finflow.notification.application.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentRejectedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentRejectedConsumer.class);

    private final NotificationService notificationService;

    public PaymentRejectedConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "payment-rejected",
            groupId = "notification-service",
            containerFactory = "paymentRejectedListenerFactory"
    )
    public void consume(ConsumerRecord<String, PaymentRejectedEvent> record) {
        var event = record.value();
        log.info("received event={} paymentId={} reason={}", event.eventType(), event.paymentId(), event.reason());
        notificationService.notifyPaymentRejected(null, event.paymentId());
    }
}
