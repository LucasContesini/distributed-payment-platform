package com.finflow.payment.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finflow.payment.application.command.CreatePaymentCommand;
import com.finflow.payment.domain.exception.InsufficientFundsException;
import com.finflow.payment.domain.outbox.OutboxEventRepository;
import com.finflow.payment.domain.payment.Payment;
import com.finflow.payment.domain.payment.PaymentRepository;
import com.finflow.payment.domain.payment.PaymentStatus;
import com.finflow.payment.infrastructure.client.WalletServiceClient;
import com.finflow.payment.infrastructure.metrics.PaymentMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentApplicationServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private OutboxEventRepository outboxRepository;
    @Mock private WalletServiceClient walletClient;
    @Mock private PaymentMetrics metrics;

    private PaymentApplicationService paymentService;

    private final UUID payerId = UUID.randomUUID();
    private final UUID payeeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        paymentService = new PaymentApplicationService(
                paymentRepository, outboxRepository, walletClient, objectMapper, metrics);
    }

    @Test
    void createPayment_WithSufficientFunds_ReturnsPendingFraudReview() {
        when(paymentRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(outboxRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(walletClient).reserve(any(), any(), any(), any());

        var command = new CreatePaymentCommand(payerId, payeeId, new BigDecimal("100.00"), "BRL", "key-001");
        var payment = paymentService.createPayment(command);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING_FRAUD_REVIEW);
        verify(walletClient).reserve(any(), eq(payerId), eq(new BigDecimal("100.00")), eq("BRL"));
        verify(metrics).recordCreated();
        verify(outboxRepository).save(any());
    }

    @Test
    void createPayment_WithInsufficientFunds_ReturnsRejected() {
        when(paymentRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(outboxRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new InsufficientFundsException()).when(walletClient).reserve(any(), any(), any(), any());

        var command = new CreatePaymentCommand(payerId, payeeId, new BigDecimal("99999.00"), "BRL", "key-002");
        var payment = paymentService.createPayment(command);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(payment.getRejectionReason()).isEqualTo("Insufficient funds");
        verify(metrics).recordCreated();
        verify(metrics).recordRejected("insufficient_funds");
    }

    @Test
    void createPayment_WithDuplicateIdempotencyKey_ReturnsExistingPayment() {
        var existing = Payment.create(payerId, payeeId, new BigDecimal("50.00"), "BRL", "key-dup");
        when(paymentRepository.findByIdempotencyKey("key-dup")).thenReturn(Optional.of(existing));

        var command = new CreatePaymentCommand(payerId, payeeId, new BigDecimal("50.00"), "BRL", "key-dup");
        var result = paymentService.createPayment(command);

        assertThat(result).isSameAs(existing);
        verifyNoInteractions(walletClient);
        verifyNoInteractions(outboxRepository);
        verifyNoInteractions(metrics);
    }
}
