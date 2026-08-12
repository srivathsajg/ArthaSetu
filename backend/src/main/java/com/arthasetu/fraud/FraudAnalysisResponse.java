package com.arthasetu.fraud;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FraudAnalysisResponse {

    private int riskScore;

    private RiskLevel riskLevel;

    private FraudDecision decision;

    private String reason;
}