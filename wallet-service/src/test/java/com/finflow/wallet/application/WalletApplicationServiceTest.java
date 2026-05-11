package com.finflow.wallet.application;

import com.finflow.wallet.application.command.CreateWalletCommand;
import com.finflow.wallet.application.command.ReserveBalanceCommand;
import com.finflow.wallet.domain.exception.InsufficientFundsException;
import com.finflow.wallet.domain.exception.WalletAlreadyExistsException;
import com.finflow.wallet.domain.exception.WalletNotFoundException;
import com.finflow.wallet.domain.wallet.Wallet;
import com.finflow.wallet.domain.wallet.WalletEntryRepository;
import com.finflow.wallet.domain.wallet.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletApplicationServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletEntryRepository entryRepository;

    @InjectMocks
    private WalletApplicationService walletService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void createWallet_WithNewUser_CreatesWalletSuccessfully() {
        when(walletRepository.existsByUserId(userId)).thenReturn(false);
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = walletService.createWallet(new CreateWalletCommand(userId, new BigDecimal("1000.00"), "BRL"));

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(result.getAvailableBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    void createWallet_WithExistingUser_ThrowsWalletAlreadyExists() {
        when(walletRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() ->
                walletService.createWallet(new CreateWalletCommand(userId, BigDecimal.TEN, "BRL"))
        ).isInstanceOf(WalletAlreadyExistsException.class);
    }

    @Test
    void reserve_WithSufficientBalance_DecreasesAvailableBalance() {
        var wallet = Wallet.create(userId, new BigDecimal("500.00"), "BRL");
        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(entryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        walletService.reserve(new ReserveBalanceCommand(UUID.randomUUID(), userId, new BigDecimal("200.00"), "BRL"));

        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("300.00");
        assertThat(wallet.getReservedBalance()).isEqualByComparingTo("200.00");
    }

    @Test
    void reserve_WithInsufficientBalance_ThrowsInsufficientFunds() {
        var wallet = Wallet.create(userId, new BigDecimal("100.00"), "BRL");
        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() ->
                walletService.reserve(new ReserveBalanceCommand(UUID.randomUUID(), userId, new BigDecimal("500.00"), "BRL"))
        ).isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void reserve_ForUnknownUser_ThrowsWalletNotFound() {
        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                walletService.reserve(new ReserveBalanceCommand(UUID.randomUUID(), userId, BigDecimal.ONE, "BRL"))
        ).isInstanceOf(WalletNotFoundException.class);
    }
}
