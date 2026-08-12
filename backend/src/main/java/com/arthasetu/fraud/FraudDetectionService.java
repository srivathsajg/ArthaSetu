package com.arthasetu.fraud;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FraudDetectionService {

    public FraudAnalysisResponse analyze(FraudAnalysisRequest request) {

        int score = 0;
        StringBuilder reason = new StringBuilder();

        BigDecimal amount = request.getAmount();

        // 1. Transaction amount
        if (amount.compareTo(BigDecimal.valueOf(100000)) > 0) {
            score += 30;
            reason.append("Very high transaction amount. ");
        } else if (amount.compareTo(BigDecimal.valueOf(50000)) >= 0) {
            score += 20;
            reason.append("High transaction amount. ");
        } else if (amount.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            score += 10;
            reason.append("Unusual transaction amount. ");
        }

        // 2. New beneficiary
        if (request.isNewBeneficiary()) {
            score += 20;
            reason.append("New beneficiary. ");
        }

        // 3. Unusual transaction time
        if (request.getTransactionHour() >= 0
                && request.getTransactionHour() <= 5) {

            score += 10;
            reason.append("Transaction made during unusual hours. ");
        }

        // 4. Rapid transactions
        if (request.isRapidTransactions()) {
            score += 15;
            reason.append("Rapid transaction activity detected. ");
        }

        // 5. Previous fraud
        if (request.isPreviousFraud()) {
            score += 30;
            reason.append("Previous fraud history detected. ");
        }

        // 6. Location anomaly
        if (request.isSuspiciousLocationChange()) {
            score += 20;
            reason.append("Suspicious location change detected. ");
        }

        // Maximum score = 100
        score = Math.min(score, 100);

        RiskLevel riskLevel = determineRiskLevel(score);

        FraudDecision decision = determineDecision(riskLevel);

        if (reason.length() == 0) {
            reason.append("No significant fraud indicators detected.");
        }

        return new FraudAnalysisResponse(
                score,
                riskLevel,
                decision,
                reason.toString().trim());
    }

    private RiskLevel determineRiskLevel(int score) {

        if (score >= 80) {
            return RiskLevel.CRITICAL;
        }

        if (score >= 60) {
            return RiskLevel.HIGH;
        }

        if (score >= 30) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    private FraudDecision determineDecision(RiskLevel riskLevel) {

        return switch (riskLevel) {

            case LOW -> FraudDecision.APPROVE;

            case MEDIUM -> FraudDecision.APPROVE_AND_MONITOR;

            case HIGH -> FraudDecision.UNDER_REVIEW;

            case CRITICAL -> FraudDecision.BLOCK;
        };
    }
}