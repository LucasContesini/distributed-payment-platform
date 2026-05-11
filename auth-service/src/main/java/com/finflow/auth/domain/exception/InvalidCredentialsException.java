package com.finflow.auth.domain.exception;

import com.finflow.domain.exception.DomainException;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
