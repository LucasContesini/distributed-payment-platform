package com.finflow.wallet.api;

import com.finflow.wallet.api.dto.InternalReleaseRequest;
import com.finflow.wallet.api.dto.InternalReserveRequest;
import com.finflow.wallet.api.dto.InternalSettleRequest;
import com.finflow.wallet.application.WalletApplicationService;
import com.finflow.wallet.application.command.ReleaseReservationCommand;
import com.finflow.wallet.application.command.ReserveBalanceCommand;
import com.finflow.wallet.application.command.SettlePaymentCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/wallets")
public class InternalWalletController {

    private final WalletApplicationService walletService;

    public InternalWalletController(WalletApplicationService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reserve(@Valid @RequestBody InternalReserveRequest request) {
        walletService.reserve(new ReserveBalanceCommand(
                request.paymentId(), request.userId(), request.amount(), request.currency()));
    }

    @PostMapping("/settle")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void settle(@Valid @RequestBody InternalSettleRequest request) {
        walletService.settle(new SettlePaymentCommand(
                request.paymentId(), request.userId(), request.amount()));
    }

    @PostMapping("/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@Valid @RequestBody InternalReleaseRequest request) {
        walletService.release(new ReleaseReservationCommand(
                request.paymentId(), request.userId(), request.amount()));
    }
}
