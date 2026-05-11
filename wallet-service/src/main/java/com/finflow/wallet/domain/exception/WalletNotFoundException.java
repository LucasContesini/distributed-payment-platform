package com.finflow.wallet.domain.exception;

import com.finflow.domain.exception.DomainException;

import java.util.UUID;

public class WalletNotFoundException extends DomainException {

    public WalletNotFoundException(UUID userId) {
        super("Wallet not found for user: %s".formatted(userId));
    }
}
