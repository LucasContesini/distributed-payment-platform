package com.finflow.payment.domain.exception;

import com.finflow.domain.exception.DomainException;

public class InsufficientFundsException extends DomainException {

    public InsufficientFundsException() {
        super("Insufficient funds to complete this payment");
    }
}
