package com.finflow.wallet.api.dto;

import com.finflow.wallet.domain.wallet.Wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        UUID userId,
        BigDecimal balance,
        BigDecimal reservedBalance,
        BigDecimal availableBalance,
        String currency,
        Instant createdAt,
        Instant updatedAt
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getReservedBalance(),
                wallet.getAvailableBalance(),
                wallet.getCurrency(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }
}
