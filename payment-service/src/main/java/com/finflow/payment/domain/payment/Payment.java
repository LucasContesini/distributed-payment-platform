package com.finflow.payment.domain.payment;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", schema = "payment")
public class Payment {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID payerId;

    @Column(nullable = false)
    private UUID payeeId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    private String rejectionReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Payment() {}

    public static Payment create(UUID payerId, UUID payeeId, BigDecimal amount, String currency, String idempotencyKey) {
        var p = new Payment();
        p.id = UUID.randomUUID();
        p.payerId = payerId;
        p.payeeId = payeeId;
        p.amount = amount;
        p.currency = currency;
        p.status = PaymentStatus.PENDING;
        p.idempotencyKey = idempotencyKey;
        p.createdAt = Instant.now();
        p.updatedAt = Instant.now();
        return p;
    }

    public void markPendingFraudReview() {
        this.status = PaymentStatus.PENDING_FRAUD_REVIEW;
        this.updatedAt = Instant.now();
    }

    public void approve() {
        this.status = PaymentStatus.APPROVED;
        this.updatedAt = Instant.now();
    }

    public void reject(String reason) {
        this.status = PaymentStatus.REJECTED;
        this.rejectionReason = reason;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getPayerId() { return payerId; }
    public UUID getPayeeId() { return payeeId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getRejectionReason() { return rejectionReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
