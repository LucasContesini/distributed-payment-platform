package com.finflow.payment.domain.exception;

import com.finflow.domain.exception.DomainException;

public class DuplicateIdempotencyKeyException extends DomainException {

    public DuplicateIdempotencyKeyException(String key) {
        super("Payment with idempotency key already exists: %s".formatted(key));
    }
}
