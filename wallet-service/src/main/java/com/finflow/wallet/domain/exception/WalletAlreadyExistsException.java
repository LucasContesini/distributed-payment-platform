package com.finflow.wallet.domain.exception;

import com.finflow.domain.exception.DomainException;

import java.util.UUID;

public class WalletAlreadyExistsException extends DomainException {

    public WalletAlreadyExistsException(UUID userId) {
        super("Wallet already exists for user: %s".formatted(userId));
    }
}
