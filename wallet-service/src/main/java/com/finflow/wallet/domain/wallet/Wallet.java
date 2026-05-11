package com.finflow.wallet.domain.wallet;

import com.finflow.wallet.domain.exception.InsufficientFundsException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets", schema = "wallet")
public class Wallet {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal reservedBalance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Version
    private Long version;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Wallet() {}

    public static Wallet create(UUID userId, BigDecimal initialBalance, String currency) {
        var wallet = new Wallet();
        wallet.id = UUID.randomUUID();
        wallet.userId = userId;
        wallet.balance = initialBalance;
        wallet.reservedBalance = BigDecimal.ZERO;
        wallet.currency = currency;
        wallet.createdAt = Instant.now();
        wallet.updatedAt = Instant.now();
        return wallet;
    }

    public void reserve(BigDecimal amount) {
        if (getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(getAvailableBalance(), amount);
        }
        this.reservedBalance = this.reservedBalance.add(amount);
        this.updatedAt = Instant.now();
    }

    public void settle(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
        this.reservedBalance = this.reservedBalance.subtract(amount);
        this.updatedAt = Instant.now();
    }

    public void release(BigDecimal amount) {
        this.reservedBalance = this.reservedBalance.subtract(amount);
        this.updatedAt = Instant.now();
    }

    public BigDecimal getAvailableBalance() {
        return balance.subtract(reservedBalance);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public BigDecimal getBalance() { return balance; }
    public BigDecimal getReservedBalance() { return reservedBalance; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
