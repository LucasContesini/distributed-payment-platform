package com.finflow.wallet.infrastructure.persistence;

import com.finflow.wallet.domain.wallet.Wallet;
import com.finflow.wallet.domain.wallet.WalletRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class WalletRepositoryAdapter implements WalletRepository {

    private final SpringDataWalletRepository delegate;

    WalletRepositoryAdapter(SpringDataWalletRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<Wallet> findByUserId(UUID userId) {
        return delegate.findByUserId(userId);
    }

    @Override
    public Optional<Wallet> findByUserIdWithLock(UUID userId) {
        return delegate.findByUserIdWithLock(userId);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return delegate.existsByUserId(userId);
    }

    @Override
    public Wallet save(Wallet wallet) {
        return delegate.save(wallet);
    }
}
