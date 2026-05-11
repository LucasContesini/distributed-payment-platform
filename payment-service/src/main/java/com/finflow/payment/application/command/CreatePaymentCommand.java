package com.finflow.payment.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentCommand(
        UUID payerId,
        UUID payeeId,
        BigDecimal amount,
        String currency,
        String idempotencyKey
) {}
