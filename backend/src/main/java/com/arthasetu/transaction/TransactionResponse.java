package com.arthasetu.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(

                Long id,

                String referenceId,

                BigDecimal amount,

                TransactionType type,

                TransactionStatus status,

                Integer riskScore,

                FraudRiskLevel riskLevel,

                String sourceAccountNumber,

                String destinationAccountNumber,

                String description,

                LocalDateTime createdAt) {

}