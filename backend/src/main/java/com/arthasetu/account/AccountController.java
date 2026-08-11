package com.arthasetu.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Create a new bank account for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            Authentication authentication,
            @Valid @RequestBody CreateAccountRequest request) {

        String email = authentication.getName();

        BankAccount account = accountService.createAccount(
                email,
                request.accountType());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(account));
    }

    /**
     * Get all bank accounts belonging to the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts(
            Authentication authentication) {

        String email = authentication.getName();

        List<AccountResponse> accounts = accountService.getMyAccounts(email)
                .stream()
                .map(AccountController::toResponse)
                .toList();

        return ResponseEntity.ok(accounts);
    }

    /**
     * Get one bank account belonging to the authenticated user.
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getMyAccount(
            Authentication authentication,
            @PathVariable String accountNumber) {

        String email = authentication.getName();

        BankAccount account = accountService.getMyAccount(
                email,
                accountNumber);

        return ResponseEntity.ok(toResponse(account));
    }

    /**
     * Convert JPA entity into API response DTO.
     */
    private static AccountResponse toResponse(
            BankAccount account) {

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getIfscCode(),
                account.getStatus(),
                account.getCreatedAt());
    }
}