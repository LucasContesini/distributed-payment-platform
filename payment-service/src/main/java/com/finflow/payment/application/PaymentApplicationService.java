package com.finflow.payment.application;

import com.finflow.payment.application.command.CreatePaymentCommand;
import com.finflow.payment.domain.exception.InsufficientFundsException;
import com.finflow.payment.domain.payment.Payment;
import com.finflow.payment.domain.payment.PaymentRepository;
import com.finflow.payment.infrastructure.client.WalletServiceClient;
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

    public PaymentApplicationService(PaymentRepository paymentRepository, WalletServiceClient walletClient) {
        this.paymentRepository = paymentRepository;
        this.walletClient = walletClient;
    }

    public Payment createPayment(CreatePaymentCommand command) {
        return paymentRepository.findByIdempotencyKey(command.idempotencyKey())
                .orElseGet(() -> processNewPayment(command));
    }

    @Transactional(readOnly = true)
    public Payment findById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new com.finflow.payment.domain.exception.PaymentNotFoundException(id));
    }

    private Payment processNewPayment(CreatePaymentCommand command) {
        var payment = Payment.create(
                command.payerId(),
                command.payeeId(),
                command.amount(),
                command.currency(),
                command.idempotencyKey()
        );
        paymentRepository.save(payment);

        try {
            walletClient.reserve(payment.getId(), payment.getPayerId(), payment.getAmount(), payment.getCurrency());
            payment.markPendingFraudReview();
            log.info("paymentId={} status=PENDING_FRAUD_REVIEW balance reserved", payment.getId());
        } catch (InsufficientFundsException e) {
            payment.reject("Insufficient funds");
            log.info("paymentId={} status=REJECTED reason=insufficient_funds", payment.getId());
        }

        return paymentRepository.save(payment);
    }
}
