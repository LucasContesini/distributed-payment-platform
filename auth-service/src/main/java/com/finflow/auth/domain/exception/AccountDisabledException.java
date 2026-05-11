package com.finflow.auth.domain.exception;

import com.finflow.domain.exception.DomainException;

public class AccountDisabledException extends DomainException {

    public AccountDisabledException() {
        super("Account is suspended or deleted");
    }
}
