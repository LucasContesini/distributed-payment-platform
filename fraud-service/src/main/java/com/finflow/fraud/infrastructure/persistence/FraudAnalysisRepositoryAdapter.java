package com.finflow.fraud.infrastructure.persistence;

import com.finflow.fraud.domain.analysis.FraudAnalysis;
import com.finflow.fraud.domain.analysis.FraudAnalysisRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class FraudAnalysisRepositoryAdapter implements FraudAnalysisRepository {

    private final SpringDataFraudAnalysisRepository delegate;

    FraudAnalysisRepositoryAdapter(SpringDataFraudAnalysisRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<FraudAnalysis> findByPaymentId(UUID paymentId) {
        return delegate.findByPaymentId(paymentId);
    }

    @Override
    public FraudAnalysis save(FraudAnalysis analysis) {
        return delegate.save(analysis);
    }
}
