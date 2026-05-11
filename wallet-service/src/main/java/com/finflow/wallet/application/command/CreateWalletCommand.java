package com.finflow.wallet.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWalletCommand(UUID userId, BigDecimal initialBalance, String currency) {}
