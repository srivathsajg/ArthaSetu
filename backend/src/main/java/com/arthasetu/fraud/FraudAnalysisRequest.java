package com.arthasetu.fraud;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FraudAnalysisRequest {

    @NotNull
    @PositiveOrZero
    private BigDecimal amount;

    private boolean newBeneficiary;

    private int transactionHour;

    private boolean rapidTransactions;

    private boolean previousFraud;

    private boolean suspiciousLocationChange;
}