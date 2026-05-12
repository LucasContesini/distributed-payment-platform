package com.finflow.audit.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finflow.audit.domain.AuditEventType;
import com.finflow.audit.domain.AuditRecord;
import com.finflow.audit.domain.AuditRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditRecordRepository repository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private AuditService auditService;

    @Test
    void record_PersistsAuditRecordWithCorrectFields() {
        var paymentId = UUID.randomUUID();
        var payload = new Object() { public final String key = "value"; };
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        auditService.record(paymentId, AuditEventType.PAYMENT_APPROVED, payload, "payment-service", Instant.now());

        var captor = ArgumentCaptor.forClass(AuditRecord.class);
        verify(repository).save(captor.capture());
        var record = captor.getValue();

        assertThat(record.getAggregateId()).isEqualTo(paymentId);
        assertThat(record.getEventType()).isEqualTo(AuditEventType.PAYMENT_APPROVED);
        assertThat(record.getSourceService()).isEqualTo("payment-service");
        assertThat(record.getPayload()).contains("value");
        assertThat(record.getRecordedAt()).isNotNull();
    }
}
