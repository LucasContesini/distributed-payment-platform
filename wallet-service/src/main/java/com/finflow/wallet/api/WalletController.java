package com.finflow.wallet.api;

import com.finflow.wallet.api.dto.CreateWalletRequest;
import com.finflow.wallet.api.dto.WalletResponse;
import com.finflow.wallet.application.WalletApplicationService;
import com.finflow.wallet.application.command.CreateWalletCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletApplicationService walletService;

    public WalletController(WalletApplicationService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse createWallet(@Valid @RequestBody CreateWalletRequest request) {
        var command = new CreateWalletCommand(request.userId(), request.initialBalance(), request.currency());
        return WalletResponse.from(walletService.createWallet(command));
    }

    @GetMapping("/{userId}")
    public WalletResponse getWallet(@PathVariable UUID userId) {
        return WalletResponse.from(walletService.findByUserId(userId));
    }
}
