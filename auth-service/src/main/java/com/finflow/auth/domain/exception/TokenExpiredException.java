package com.finflow.auth.domain.exception;

import com.finflow.domain.exception.DomainException;

public class TokenExpiredException extends DomainException {

    public TokenExpiredException() {
        super("Refresh token is expired or invalid");
    }
}
