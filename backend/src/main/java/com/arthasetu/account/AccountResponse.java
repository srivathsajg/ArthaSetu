package com.arthasetu.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String accountNumber,
        AccountType accountType,
        BigDecimal balance,
        String ifscCode,
        AccountStatus status,
        LocalDateTime createdAt) {
}