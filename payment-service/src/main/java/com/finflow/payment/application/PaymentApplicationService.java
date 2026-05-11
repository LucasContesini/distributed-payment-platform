package com.finflow.payment.application;

import com.finflow.events.fraud.FraudAnalysisCompletedEvent;
import com.finflow.payment.application.command.CreatePaymentCommand;
import com.finflow.payment.domain.exception.InsufficientFundsException;
import com.finflow.payment.domain.exception.PaymentNotFoundException;
import com.finflow.payment.domain.payment.Payment;
import com.finflow.payment.domain.payment.PaymentRepository;
import com.finflow.payment.infrastructure.client.WalletServiceClient;
import com.finflow.payment.infrastructure.kafka.PaymentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class PaymentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);

    private final PaymentRepository paymentRepository;
    private final WalletServiceClient walletClient;
    private final PaymentEventPublisher eventPublisher;

    public PaymentApplicationService(
            PaymentRepository paymentRepository,
            WalletServiceClient walletClient,
            PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.walletClient = walletClient;
        this.eventPublisher = eventPublisher;
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
                eventPublisher.publishPaymentApproved(payment);
                log.info("paymentId={} status=APPROVED", payment.getId());
            }
            case REJECTED -> {
                walletClient.release(payment.getId(), payment.getPayerId(), payment.getAmount());
                payment.reject(event.reason());
                paymentRepository.save(payment);
                eventPublisher.publishPaymentRejected(payment);
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
            eventPublisher.publishFraudAnalysisRequested(payment);
            log.info("paymentId={} status=PENDING_FRAUD_REVIEW", payment.getId());
        } catch (InsufficientFundsException e) {
            payment.reject("Insufficient funds");
            paymentRepository.save(payment);
            eventPublisher.publishPaymentRejected(payment);
            log.info("paymentId={} status=REJECTED reason=insufficient_funds", payment.getId());
        }

        return payment;
    }
}
