package com.finflow.wallet.application;

import com.finflow.wallet.application.command.CreateWalletCommand;
import com.finflow.wallet.application.command.ReleaseReservationCommand;
import com.finflow.wallet.application.command.ReserveBalanceCommand;
import com.finflow.wallet.application.command.SettlePaymentCommand;
import com.finflow.wallet.domain.exception.WalletAlreadyExistsException;
import com.finflow.wallet.domain.exception.WalletNotFoundException;
import com.finflow.wallet.domain.wallet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class WalletApplicationService {

    private static final Logger log = LoggerFactory.getLogger(WalletApplicationService.class);

    private final WalletRepository walletRepository;
    private final WalletEntryRepository entryRepository;

    public WalletApplicationService(WalletRepository walletRepository, WalletEntryRepository entryRepository) {
        this.walletRepository = walletRepository;
        this.entryRepository = entryRepository;
    }

    public Wallet createWallet(CreateWalletCommand command) {
        if (walletRepository.existsByUserId(command.userId())) {
            throw new WalletAlreadyExistsException(command.userId());
        }
        var wallet = Wallet.create(command.userId(), command.initialBalance(), command.currency());
        return walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public Wallet findByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
    }

    public void reserve(ReserveBalanceCommand command) {
        var wallet = walletRepository.findByUserIdWithLock(command.userId())
                .orElseThrow(() -> new WalletNotFoundException(command.userId()));

        wallet.reserve(command.amount());
        walletRepository.save(wallet);

        entryRepository.save(WalletEntry.of(wallet.getId(), command.paymentId(), EntryType.RESERVATION, command.amount()));
        log.info("walletId={} paymentId={} amount={} type=RESERVATION", wallet.getId(), command.paymentId(), command.amount());
    }

    public void settle(SettlePaymentCommand command) {
        var wallet = walletRepository.findByUserIdWithLock(command.userId())
                .orElseThrow(() -> new WalletNotFoundException(command.userId()));

        wallet.settle(command.amount());
        walletRepository.save(wallet);

        entryRepository.save(WalletEntry.of(wallet.getId(), command.paymentId(), EntryType.SETTLEMENT, command.amount()));
        log.info("walletId={} paymentId={} amount={} type=SETTLEMENT", wallet.getId(), command.paymentId(), command.amount());
    }

    public void release(ReleaseReservationCommand command) {
        var wallet = walletRepository.findByUserIdWithLock(command.userId())
                .orElseThrow(() -> new WalletNotFoundException(command.userId()));

        wallet.release(command.amount());
        walletRepository.save(wallet);

        entryRepository.save(WalletEntry.of(wallet.getId(), command.paymentId(), EntryType.RELEASE, command.amount()));
        log.info("walletId={} paymentId={} amount={} type=RELEASE", wallet.getId(), command.paymentId(), command.amount());
    }
}
