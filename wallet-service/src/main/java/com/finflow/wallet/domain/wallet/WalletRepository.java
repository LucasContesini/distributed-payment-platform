package com.finflow.wallet.domain.wallet;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    Optional<Wallet> findByUserId(UUID userId);
    Optional<Wallet> findByUserIdWithLock(UUID userId);
    boolean existsByUserId(UUID userId);
    Wallet save(Wallet wallet);
}
