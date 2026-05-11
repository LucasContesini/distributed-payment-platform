package com.finflow.wallet.domain.wallet;

import java.util.UUID;

public interface WalletEntryRepository {
    WalletEntry save(WalletEntry entry);
    boolean existsByPaymentIdAndType(UUID paymentId, EntryType type);
}
