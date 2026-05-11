package com.finflow.payment.api.dto;

import com.finflow.payment.domain.payment.Payment;
import com.finflow.payment.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID payerId,
        UUID payeeId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String idempotencyKey,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPayerId(),
                payment.getPayeeId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getIdempotencyKey(),
                payment.getRejectionReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
