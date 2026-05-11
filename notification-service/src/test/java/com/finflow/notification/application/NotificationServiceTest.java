package com.finflow.notification.application;

import com.finflow.notification.domain.notification.NotificationLog;
import com.finflow.notification.domain.notification.NotificationLogRepository;
import com.finflow.notification.domain.notification.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationLogRepository repository;

    @InjectMocks
    private NotificationService notificationService;

    private final UUID recipientId = UUID.randomUUID();
    private final UUID paymentId   = UUID.randomUUID();

    @Test
    void notifyPaymentApproved_PersistsLogWithCorrectType() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyPaymentApproved(recipientId, paymentId);

        var captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.PAYMENT_APPROVED);
        assertThat(captor.getValue().getPaymentId()).isEqualTo(paymentId);
    }

    @Test
    void notifyPaymentRejected_PersistsLogWithCorrectType() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyPaymentRejected(recipientId, paymentId);

        var captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.PAYMENT_REJECTED);
    }
}
