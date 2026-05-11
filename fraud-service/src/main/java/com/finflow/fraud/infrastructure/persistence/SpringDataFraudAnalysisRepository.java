package com.finflow.fraud.infrastructure.persistence;

import com.finflow.fraud.domain.analysis.FraudAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataFraudAnalysisRepository extends JpaRepository<FraudAnalysis, UUID> {
    Optional<FraudAnalysis> findByPaymentId(UUID paymentId);
}
