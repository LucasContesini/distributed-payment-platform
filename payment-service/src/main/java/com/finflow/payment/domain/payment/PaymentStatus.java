package com.finflow.payment.domain.payment;

public enum PaymentStatus {
    PENDING,
    PENDING_FRAUD_REVIEW,
    APPROVED,
    REJECTED,
    CANCELLED
}
