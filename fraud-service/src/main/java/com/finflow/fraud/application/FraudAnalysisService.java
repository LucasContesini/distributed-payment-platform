package com.finflow.fraud.application;

import com.finflow.fraud.domain.analysis.FraudAnalysis;
import com.finflow.fraud.domain.analysis.FraudAnalysisRepository;
import com.finflow.fraud.domain.analysis.FraudDecision;
import com.finflow.fraud.infrastructure.kafka.FraudEventPublisher;
import com.finflow.fraud.infrastructure.metrics.FraudMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class FraudAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(FraudAnalysisService.class);
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("50000");

    private final FraudAnalysisRepository repository;
    private final FraudEventPublisher eventPublisher;
    private final FraudMetrics metrics;

    public FraudAnalysisService(FraudAnalysisRepository repository, FraudEventPublisher eventPublisher, FraudMetrics metrics) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.metrics = metrics;
    }

    public void analyze(UUID paymentId, UUID payerId, BigDecimal amount, String currency) {
        if (repository.findByPaymentId(paymentId).isPresent()) {
            log.warn("Duplicate analysis request for paymentId={} — skipping (idempotent)", paymentId);
            return;
        }

        var decision = evaluate(amount);
        var reason = decision == FraudDecision.APPROVED
                ? "All checks passed"
                : "Amount exceeds high-value threshold (%s)".formatted(HIGH_VALUE_THRESHOLD);

        var analysis = FraudAnalysis.create(paymentId, payerId, amount, decision, reason);
        repository.save(analysis);

        eventPublisher.publishFraudAnalysisCompleted(paymentId, decision, reason);
        metrics.recordAnalysis(decision);
        log.info("paymentId={} fraudDecision={} reason={}", paymentId, decision, reason);
    }

    private FraudDecision evaluate(BigDecimal amount) {
        if (amount.compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            return FraudDecision.REJECTED;
        }
        return FraudDecision.APPROVED;
    }
}
