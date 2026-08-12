package com.arthasetu.account;

import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(

        @NotNull(message = "Account type is required") AccountType accountType

) {
}