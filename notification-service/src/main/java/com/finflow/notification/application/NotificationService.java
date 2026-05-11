package com.finflow.notification.application;

import com.finflow.notification.domain.notification.NotificationLog;
import com.finflow.notification.domain.notification.NotificationLogRepository;
import com.finflow.notification.domain.notification.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationLogRepository repository;

    public NotificationService(NotificationLogRepository repository) {
        this.repository = repository;
    }

    public void notifyPaymentApproved(UUID recipientId, UUID paymentId) {
        log.info("channel=EMAIL type=PAYMENT_APPROVED recipientId={} paymentId={}", recipientId, paymentId);
        repository.save(NotificationLog.of(recipientId, paymentId, NotificationType.PAYMENT_APPROVED));
    }

    public void notifyPaymentRejected(UUID recipientId, UUID paymentId) {
        log.info("channel=EMAIL type=PAYMENT_REJECTED recipientId={} paymentId={}", recipientId, paymentId);
        repository.save(NotificationLog.of(recipientId, paymentId, NotificationType.PAYMENT_REJECTED));
    }
}
