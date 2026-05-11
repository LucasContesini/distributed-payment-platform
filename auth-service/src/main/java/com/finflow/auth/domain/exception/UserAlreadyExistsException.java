package com.finflow.auth.domain.exception;

import com.finflow.domain.exception.DomainException;

public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(String email) {
        super("User already exists: %s".formatted(email));
    }
}
