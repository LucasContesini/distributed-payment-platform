package com.finflow.domain.exception;

public class NotFoundException extends DomainException {

    public NotFoundException(String resource, Object id) {
        super("%s not found: %s".formatted(resource, id));
    }
}
