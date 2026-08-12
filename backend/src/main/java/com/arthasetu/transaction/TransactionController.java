package com.arthasetu.transaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

        private final TransactionService transactionService;

        /**
         * Deposit money into an account.
         */
        @PostMapping("/deposit")
        public ResponseEntity<TransactionResponse> deposit(
                        Authentication authentication,
                        @Valid @RequestBody TransactionRequest request) {

                String email = authentication.getName();

                Transaction transaction = transactionService.deposit(
                                email,
                                request.accountNumber(),
                                request.amount(),
                                request.description());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(toResponse(transaction));
        }

        /**
         * Withdraw money from an account.
         */
        @PostMapping("/withdraw")
        public ResponseEntity<TransactionResponse> withdraw(
                        Authentication authentication,
                        @Valid @RequestBody TransactionRequest request) {

                String email = authentication.getName();

                Transaction transaction = transactionService.withdraw(
                                email,
                                request.accountNumber(),
                                request.amount(),
                                request.description());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(toResponse(transaction));
        }

        /**
         * Transfer money between two bank accounts.
         */
        @PostMapping("/transfer")
        public ResponseEntity<TransactionResponse> transfer(
                        Authentication authentication,
                        @Valid @RequestBody TransferRequest request) {

                String email = authentication.getName();

                Transaction transaction = transactionService.transfer(
                                email,
                                request.sourceAccountNumber(),
                                request.destinationAccountNumber(),
                                request.amount(),
                                request.description());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(toResponse(transaction));
        }

        /**
         * Get transaction history for one of the user's accounts.
         */
        @GetMapping("/{accountNumber}")
        public ResponseEntity<List<TransactionResponse>> getTransactions(
                        Authentication authentication,
                        @PathVariable String accountNumber) {

                String email = authentication.getName();

                List<TransactionResponse> transactions = transactionService
                                .getMyTransactions(email, accountNumber)
                                .stream()
                                .map(TransactionController::toResponse)
                                .toList();

                return ResponseEntity.ok(transactions);
        }

        /**
         * Convert Transaction entity into API response.
         */
        private static TransactionResponse toResponse(
                        Transaction transaction) {

                String sourceAccountNumber = transaction.getSourceAccount() != null
                                ? transaction.getSourceAccount().getAccountNumber()
                                : null;

                String destinationAccountNumber = transaction.getDestinationAccount() != null
                                ? transaction.getDestinationAccount().getAccountNumber()
                                : null;

                return new TransactionResponse(
                                transaction.getId(),
                                transaction.getReferenceId(),
                                transaction.getAmount(),
                                transaction.getType(),
                                transaction.getStatus(),
                                transaction.getRiskScore(),
                                transaction.getRiskLevel(),
                                sourceAccountNumber,
                                destinationAccountNumber,
                                transaction.getDescription(),
                                transaction.getCreatedAt());
        }
}