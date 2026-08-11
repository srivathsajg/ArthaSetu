package com.arthasetu.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(

        @NotBlank(message = "Source account number is required") String sourceAccountNumber,

        @NotBlank(message = "Destination account number is required") String destinationAccountNumber,

        @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount,

        String description

) {
}