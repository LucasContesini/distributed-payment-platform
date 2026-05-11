package com.finflow.payment.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finflow.events.fraud.FraudAnalysisRequestedEvent;
import com.finflow.payment.domain.outbox.OutboxEvent;
import com.finflow.payment.domain.outbox.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelayServiceTest {

    @Mock
    private OutboxEventRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private OutboxRelayService relayService;

    @BeforeEach
    void setUp() {
        var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        relayService = new OutboxRelayService(outboxRepository, kafkaTemplate, objectMapper);
    }

    @Test
    void relay_WithUnpublishedEvent_PublishesAndMarksAsPublished() throws Exception {
        var domainEvent = new FraudAnalysisRequestedEvent(
                UUID.randomUUID(), Instant.now(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("500.00"), "BRL");
        var payload = new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(domainEvent);

        var outboxEvent = OutboxEvent.of(domainEvent.paymentId(), "fraud.analysis.requested", "fraud-analysis-requested", payload);

        when(outboxRepository.findUnpublishedWithLock(100)).thenReturn(List.of(outboxEvent));
        when(outboxRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        relayService.relay();

        assertThat(outboxEvent.isPublished()).isTrue();
        assertThat(outboxEvent.getPublishedAt()).isNotNull();
        verify(kafkaTemplate).send(eq("fraud-analysis-requested"), any(), any(FraudAnalysisRequestedEvent.class));
        verify(outboxRepository).save(outboxEvent);
    }

    @Test
    void relay_WithNoUnpublishedEvents_DoesNothing() {
        when(outboxRepository.findUnpublishedWithLock(100)).thenReturn(List.of());

        relayService.relay();

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void relay_WhenKafkaFails_LeavesEventUnpublished() throws Exception {
        var domainEvent = new FraudAnalysisRequestedEvent(
                UUID.randomUUID(), Instant.now(), UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.TEN, "BRL");
        var payload = new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(domainEvent);
        var outboxEvent = OutboxEvent.of(domainEvent.paymentId(), "fraud.analysis.requested", "fraud-analysis-requested", payload);

        when(outboxRepository.findUnpublishedWithLock(100)).thenReturn(List.of(outboxEvent));
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka unavailable")));

        relayService.relay();

        assertThat(outboxEvent.isPublished()).isFalse();
        verify(outboxRepository, never()).save(any());
    }
}
