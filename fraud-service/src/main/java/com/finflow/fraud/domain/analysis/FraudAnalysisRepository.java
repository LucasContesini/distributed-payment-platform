package com.finflow.fraud.domain.analysis;

import java.util.Optional;
import java.util.UUID;

public interface FraudAnalysisRepository {
    Optional<FraudAnalysis> findByPaymentId(UUID paymentId);
    FraudAnalysis save(FraudAnalysis analysis);
}
