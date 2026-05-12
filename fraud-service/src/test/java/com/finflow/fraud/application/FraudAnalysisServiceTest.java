package com.finflow.fraud.application;

import com.finflow.fraud.domain.analysis.FraudAnalysis;
import com.finflow.fraud.domain.analysis.FraudAnalysisRepository;
import com.finflow.fraud.domain.analysis.FraudDecision;
import com.finflow.fraud.infrastructure.kafka.FraudEventPublisher;
import com.finflow.fraud.infrastructure.metrics.FraudMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudAnalysisServiceTest {

    @Mock
    private FraudAnalysisRepository repository;

    @Mock
    private FraudEventPublisher eventPublisher;

    @Mock
    private FraudMetrics metrics;

    @InjectMocks
    private FraudAnalysisService fraudService;

    private final UUID paymentId = UUID.randomUUID();
    private final UUID payerId = UUID.randomUUID();

    @Test
    void analyze_AmountBelowThreshold_Approves() {
        when(repository.findByPaymentId(paymentId)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        fraudService.analyze(paymentId, payerId, new BigDecimal("1000.00"), "BRL");

        var captor = ArgumentCaptor.forClass(FraudAnalysis.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDecision()).isEqualTo(FraudDecision.APPROVED);
        verify(eventPublisher).publishFraudAnalysisCompleted(paymentId, FraudDecision.APPROVED, "All checks passed");
        verify(metrics).recordAnalysis(FraudDecision.APPROVED);
    }

    @Test
    void analyze_AmountAboveThreshold_Rejects() {
        when(repository.findByPaymentId(paymentId)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        fraudService.analyze(paymentId, payerId, new BigDecimal("100000.00"), "BRL");

        var captor = ArgumentCaptor.forClass(FraudAnalysis.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDecision()).isEqualTo(FraudDecision.REJECTED);
        verify(eventPublisher).publishFraudAnalysisCompleted(eq(paymentId), eq(FraudDecision.REJECTED), any());
        verify(metrics).recordAnalysis(FraudDecision.REJECTED);
    }

    @Test
    void analyze_DuplicateRequest_SkipsIdempotently() {
        var existing = FraudAnalysis.create(paymentId, payerId, BigDecimal.TEN, FraudDecision.APPROVED, "ok");
        when(repository.findByPaymentId(paymentId)).thenReturn(Optional.of(existing));

        fraudService.analyze(paymentId, payerId, BigDecimal.TEN, "BRL");

        verify(repository, never()).save(any());
        verifyNoInteractions(eventPublisher);
        verifyNoInteractions(metrics);
    }
}
