package com.finflow.wallet.infrastructure.persistence;

import com.finflow.wallet.domain.wallet.EntryType;
import com.finflow.wallet.domain.wallet.WalletEntry;
import com.finflow.wallet.domain.wallet.WalletEntryRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class WalletEntryRepositoryAdapter implements WalletEntryRepository {

    private final SpringDataWalletEntryRepository delegate;

    WalletEntryRepositoryAdapter(SpringDataWalletEntryRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public WalletEntry save(WalletEntry entry) {
        return delegate.save(entry);
    }

    @Override
    public boolean existsByPaymentIdAndType(UUID paymentId, EntryType type) {
        return delegate.existsByPaymentIdAndType(paymentId, type);
    }
}
