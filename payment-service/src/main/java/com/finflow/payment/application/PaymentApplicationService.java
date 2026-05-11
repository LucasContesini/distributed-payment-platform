package com.finflow.payment.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.events.fraud.FraudAnalysisCompletedEvent;
import com.finflow.events.fraud.FraudAnalysisRequestedEvent;
import com.finflow.events.payment.PaymentApprovedEvent;
import com.finflow.events.payment.PaymentRejectedEvent;
import com.finflow.payment.application.command.CreatePaymentCommand;
import com.finflow.payment.domain.exception.InsufficientFundsException;
import com.finflow.payment.domain.exception.PaymentNotFoundException;
import com.finflow.payment.domain.outbox.OutboxEvent;
import com.finflow.payment.domain.outbox.OutboxEventRepository;
import com.finflow.payment.domain.payment.Payment;
import com.finflow.payment.domain.payment.PaymentRepository;
import com.finflow.payment.infrastructure.client.WalletServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class PaymentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxRepository;
    private final WalletServiceClient walletClient;
    private final ObjectMapper objectMapper;

    public PaymentApplicationService(
            PaymentRepository paymentRepository,
            OutboxEventRepository outboxRepository,
            WalletServiceClient walletClient,
            ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.outboxRepository = outboxRepository;
        this.walletClient = walletClient;
        this.objectMapper = objectMapper;
    }

    public Payment createPayment(CreatePaymentCommand command) {
        return paymentRepository.findByIdempotencyKey(command.idempotencyKey())
                .orElseGet(() -> processNewPayment(command));
    }

    @Transactional(readOnly = true)
    public Payment findById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    public void processFraudResult(FraudAnalysisCompletedEvent event) {
        var payment = paymentRepository.findById(event.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(event.paymentId()));

        switch (event.decision()) {
            case APPROVED -> {
                walletClient.settle(payment.getId(), payment.getPayerId(), payment.getAmount());
                payment.approve();
                paymentRepository.save(payment);
                enqueue(payment.getId(), "payment.approved", "payment-approved",
                        new PaymentApprovedEvent(UUID.randomUUID(), Instant.now(), payment.getId(), payment.getPayerId()));
                log.info("paymentId={} status=APPROVED", payment.getId());
            }
            case REJECTED -> {
                walletClient.release(payment.getId(), payment.getPayerId(), payment.getAmount());
                payment.reject(event.reason());
                paymentRepository.save(payment);
                enqueue(payment.getId(), "payment.rejected", "payment-rejected",
                        new PaymentRejectedEvent(UUID.randomUUID(), Instant.now(), payment.getId(), event.reason()));
                log.info("paymentId={} status=REJECTED reason={}", payment.getId(), event.reason());
            }
        }
    }

    private Payment processNewPayment(CreatePaymentCommand command) {
        var payment = Payment.create(
                command.payerId(), command.payeeId(),
                command.amount(), command.currency(),
                command.idempotencyKey());
        paymentRepository.save(payment);

        try {
            walletClient.reserve(payment.getId(), payment.getPayerId(), payment.getAmount(), payment.getCurrency());
            payment.markPendingFraudReview();
            paymentRepository.save(payment);
            enqueue(payment.getId(), "fraud.analysis.requested", "fraud-analysis-requested",
                    new FraudAnalysisRequestedEvent(UUID.randomUUID(), Instant.now(),
                            payment.getId(), payment.getPayerId(), payment.getAmount(), payment.getCurrency()));
            log.info("paymentId={} status=PENDING_FRAUD_REVIEW outbox queued", payment.getId());
        } catch (InsufficientFundsException e) {
            payment.reject("Insufficient funds");
            paymentRepository.save(payment);
            enqueue(payment.getId(), "payment.rejected", "payment-rejected",
                    new PaymentRejectedEvent(UUID.randomUUID(), Instant.now(), payment.getId(), "Insufficient funds"));
            log.info("paymentId={} status=REJECTED reason=insufficient_funds", payment.getId());
        }

        return payment;
    }

    private void enqueue(UUID aggregateId, String eventType, String topic, Object event) {
        try {
            outboxRepository.save(OutboxEvent.of(aggregateId, eventType, topic, objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox event", e);
        }
    }
}
