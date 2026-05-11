package com.finflow.payment.infrastructure.persistence;

import com.finflow.payment.domain.payment.Payment;
import com.finflow.payment.domain.payment.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class PaymentRepositoryAdapter implements PaymentRepository {

    private final SpringDataPaymentRepository delegate;

    PaymentRepositoryAdapter(SpringDataPaymentRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return delegate.findById(id);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return delegate.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Payment save(Payment payment) {
        return delegate.save(payment);
    }
}
