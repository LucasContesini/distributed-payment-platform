package com.finflow.wallet.domain.wallet;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet_entries", schema = "wallet")
public class WalletEntry {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID walletId;

    @Column(nullable = false)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant createdAt;

    protected WalletEntry() {}

    public static WalletEntry of(UUID walletId, UUID paymentId, EntryType type, BigDecimal amount) {
        var entry = new WalletEntry();
        entry.id = UUID.randomUUID();
        entry.walletId = walletId;
        entry.paymentId = paymentId;
        entry.type = type;
        entry.amount = amount;
        entry.createdAt = Instant.now();
        return entry;
    }

    public UUID getId() { return id; }
    public UUID getWalletId() { return walletId; }
    public UUID getPaymentId() { return paymentId; }
    public EntryType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }
}
