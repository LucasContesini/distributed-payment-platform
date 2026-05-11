package com.finflow.payment.domain.exception;

import com.finflow.domain.exception.DomainException;

import java.util.UUID;

public class PaymentNotFoundException extends DomainException {

    public PaymentNotFoundException(UUID id) {
        super("Payment not found: %s".formatted(id));
    }
}
