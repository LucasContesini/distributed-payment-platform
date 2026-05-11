package com.finflow.fraud.domain.analysis;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fraud_analyses", schema = "fraud")
public class FraudAnalysis {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID paymentId;

    @Column(nullable = false)
    private UUID payerId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudDecision decision;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Instant analyzedAt;

    protected FraudAnalysis() {}

    public static FraudAnalysis create(UUID paymentId, UUID payerId, BigDecimal amount, FraudDecision decision, String reason) {
        var analysis = new FraudAnalysis();
        analysis.id = UUID.randomUUID();
        analysis.paymentId = paymentId;
        analysis.payerId = payerId;
        analysis.amount = amount;
        analysis.decision = decision;
        analysis.reason = reason;
        analysis.analyzedAt = Instant.now();
        return analysis;
    }

    public UUID getId() { return id; }
    public UUID getPaymentId() { return paymentId; }
    public UUID getPayerId() { return payerId; }
    public BigDecimal getAmount() { return amount; }
    public FraudDecision getDecision() { return decision; }
    public String getReason() { return reason; }
    public Instant getAnalyzedAt() { return analyzedAt; }
}
