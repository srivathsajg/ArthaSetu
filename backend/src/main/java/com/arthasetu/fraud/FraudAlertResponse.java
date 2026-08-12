package com.arthasetu.fraud;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FraudAlertResponse(

        Long id,

        Long transactionId,

        String referenceId,

        BigDecimal amount,

        Integer riskScore,

        RiskLevel riskLevel,

        String reason,

        FraudAlertStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}