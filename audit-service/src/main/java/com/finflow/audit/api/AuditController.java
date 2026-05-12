package com.finflow.audit.api;

import com.finflow.audit.application.AuditService;
import com.finflow.audit.domain.AuditRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/payments/{paymentId}")
    public List<AuditRecord> getAuditTrail(@PathVariable UUID paymentId) {
        return auditService.findByAggregateId(paymentId);
    }
}
