package com.finflow.wallet.domain.exception;

import com.finflow.domain.exception.DomainException;

import java.math.BigDecimal;

public class InsufficientFundsException extends DomainException {

    public InsufficientFundsException(BigDecimal available, BigDecimal required) {
        super("Insufficient funds: available=%s required=%s".formatted(available, required));
    }
}
