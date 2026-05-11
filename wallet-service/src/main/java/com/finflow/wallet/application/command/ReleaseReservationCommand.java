package com.finflow.wallet.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record ReleaseReservationCommand(UUID paymentId, UUID userId, BigDecimal amount) {}
