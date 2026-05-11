package com.finflow.wallet.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWalletRequest(
        @NotNull UUID userId,
        @NotNull @DecimalMin("0.00") @Digits(integer = 15, fraction = 4) BigDecimal initialBalance,
        @NotBlank @Size(min = 3, max = 3) String currency
) {}
