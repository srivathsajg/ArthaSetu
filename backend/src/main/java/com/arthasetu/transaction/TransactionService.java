package com.arthasetu.transaction;

import com.arthasetu.account.AccountStatus;
import com.arthasetu.account.BankAccount;
import com.arthasetu.account.BankAccountRepository;
import com.arthasetu.fraud.FraudAlertService;
import com.arthasetu.fraud.FraudAnalysisRequest;
import com.arthasetu.fraud.FraudAnalysisResponse;
import com.arthasetu.fraud.FraudDecision;
import com.arthasetu.fraud.FraudDetectionService;
import com.arthasetu.user.User;
import com.arthasetu.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

        private final TransactionRepository transactionRepository;
        private final BankAccountRepository bankAccountRepository;
        private final UserRepository userRepository;

        private final FraudDetectionService fraudDetectionService;
        private final FraudAlertService fraudAlertService;

        /**
         * Deposit money into the authenticated user's account.
         */
        @Transactional
        public Transaction deposit(
                        String email,
                        String accountNumber,
                        BigDecimal amount,
                        String description) {

                validateAmount(amount);

                BankAccount account = getUserAccount(email, accountNumber);

                validateActiveAccount(account);

                account.setBalance(
                                account.getBalance().add(amount));

                bankAccountRepository.save(account);

                Transaction transaction = Transaction.builder()
                                .referenceId(generateReferenceId())
                                .amount(amount)
                                .type(TransactionType.DEPOSIT)
                                .status(TransactionStatus.COMPLETED)
                                .destinationAccount(account)
                                .description(description)
                                .riskScore(0)
                                .riskLevel(FraudRiskLevel.LOW)
                                .createdAt(LocalDateTime.now())
                                .build();

                return transactionRepository.save(transaction);
        }

        /**
         * Withdraw money from the authenticated user's account.
         */
        @Transactional
        public Transaction withdraw(
                        String email,
                        String accountNumber,
                        BigDecimal amount,
                        String description) {

                validateAmount(amount);

                BankAccount account = getUserAccount(email, accountNumber);

                validateActiveAccount(account);

                if (account.getBalance().compareTo(amount) < 0) {
                        throw new IllegalArgumentException(
                                        "Insufficient account balance");
                }

                account.setBalance(
                                account.getBalance().subtract(amount));

                bankAccountRepository.save(account);

                Transaction transaction = Transaction.builder()
                                .referenceId(generateReferenceId())
                                .amount(amount)
                                .type(TransactionType.WITHDRAWAL)
                                .status(TransactionStatus.COMPLETED)
                                .sourceAccount(account)
                                .description(description)
                                .riskScore(0)
                                .riskLevel(FraudRiskLevel.LOW)
                                .createdAt(LocalDateTime.now())
                                .build();

                return transactionRepository.save(transaction);
        }

        /**
         * Transfer money from the authenticated user's account
         * to another active account.
         *
         * Fraud detection is performed before the transfer is completed.
         */
        @Transactional
        public Transaction transfer(
                        String email,
                        String sourceAccountNumber,
                        String destinationAccountNumber,
                        BigDecimal amount,
                        String description) {

                validateAmount(amount);

                // Source and destination cannot be the same account.
                if (sourceAccountNumber.equals(destinationAccountNumber)) {
                        throw new IllegalArgumentException(
                                        "Source and destination accounts must be different");
                }

                // Get authenticated user's source account.
                BankAccount sourceAccount = getUserAccount(email, sourceAccountNumber);

                // Get destination account.
                BankAccount destinationAccount = bankAccountRepository
                                .findByAccountNumber(destinationAccountNumber)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Destination account not found"));

                // Both accounts must be active.
                validateActiveAccount(sourceAccount);
                validateActiveAccount(destinationAccount);

                // Check source account balance.
                if (sourceAccount.getBalance().compareTo(amount) < 0) {
                        throw new IllegalArgumentException(
                                        "Insufficient account balance");
                }

                /*
                 * ---------------------------------------------------------
                 * FRAUD ANALYSIS
                 * ---------------------------------------------------------
                 *
                 * At this stage we use the signals currently available
                 * in the transfer API.
                 */
                FraudAnalysisRequest fraudRequest = new FraudAnalysisRequest(
                                amount,
                                false,
                                LocalDateTime.now().getHour(),
                                false,
                                false,
                                false);

                FraudAnalysisResponse fraudResult = fraudDetectionService.analyze(fraudRequest);

                /*
                 * Convert fraud RiskLevel into transaction FraudRiskLevel.
                 */
                FraudRiskLevel transactionRiskLevel = FraudRiskLevel.valueOf(
                                fraudResult.getRiskLevel().name());

                /*
                 * ---------------------------------------------------------
                 * CRITICAL FRAUD
                 * ---------------------------------------------------------
                 *
                 * If the fraud engine decides to BLOCK the transaction,
                 * do not move any money.
                 */
                if (fraudResult.getDecision() == FraudDecision.BLOCK) {

                        Transaction blockedTransaction = Transaction.builder()
                                        .referenceId(generateReferenceId())
                                        .amount(amount)
                                        .type(TransactionType.TRANSFER)
                                        .status(TransactionStatus.BLOCKED)
                                        .sourceAccount(sourceAccount)
                                        .destinationAccount(destinationAccount)
                                        .riskScore(fraudResult.getRiskScore())
                                        .riskLevel(transactionRiskLevel)
                                        .description(
                                                        buildFraudDescription(
                                                                        description,
                                                                        fraudResult))
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        Transaction savedTransaction = transactionRepository.save(blockedTransaction);

                        // Create fraud alert for the blocked transaction.
                        fraudAlertService.createAlert(
                                        savedTransaction,
                                        fraudResult);

                        return savedTransaction;
                }

                /*
                 * ---------------------------------------------------------
                 * NORMAL / MONITORED TRANSFER
                 * ---------------------------------------------------------
                 */

                // Remove money from source account.
                sourceAccount.setBalance(
                                sourceAccount.getBalance().subtract(amount));

                // Add money to destination account.
                destinationAccount.setBalance(
                                destinationAccount.getBalance().add(amount));

                // Save both account balances.
                bankAccountRepository.save(sourceAccount);
                bankAccountRepository.save(destinationAccount);

                /*
                 * Build transaction.
                 */
                Transaction transaction = Transaction.builder()
                                .referenceId(generateReferenceId())
                                .amount(amount)
                                .type(TransactionType.TRANSFER)
                                .status(TransactionStatus.COMPLETED)
                                .sourceAccount(sourceAccount)
                                .destinationAccount(destinationAccount)
                                .riskScore(fraudResult.getRiskScore())
                                .riskLevel(transactionRiskLevel)
                                .description(
                                                buildFraudDescription(
                                                                description,
                                                                fraudResult))
                                .createdAt(LocalDateTime.now())
                                .build();

                Transaction savedTransaction = transactionRepository.save(transaction);

                /*
                 * Create fraud alert for MEDIUM and HIGH risk.
                 *
                 * LOW risk transactions do not create alerts.
                 */
                if (fraudResult.getRiskLevel() != com.arthasetu.fraud.RiskLevel.LOW) {

                        fraudAlertService.createAlert(
                                        savedTransaction,
                                        fraudResult);
                }

                return savedTransaction;
        }

        /**
         * Get all transactions involving the authenticated user's account.
         */
        @Transactional(readOnly = true)
        public List<Transaction> getMyTransactions(
                        String email,
                        String accountNumber) {

                BankAccount account = getUserAccount(email, accountNumber);

                List<Transaction> sentTransactions = transactionRepository
                                .findBySourceAccountOrderByCreatedAtDesc(account);

                List<Transaction> receivedTransactions = transactionRepository
                                .findByDestinationAccountOrderByCreatedAtDesc(account);

                sentTransactions.addAll(receivedTransactions);

                sentTransactions.sort(
                                (a, b) -> b.getCreatedAt()
                                                .compareTo(a.getCreatedAt()));

                return sentTransactions;
        }

        /**
         * Find an account and verify that it belongs to
         * the authenticated user.
         */
        private BankAccount getUserAccount(
                        String email,
                        String accountNumber) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "User not found"));

                BankAccount account = bankAccountRepository
                                .findByAccountNumber(accountNumber)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Bank account not found"));

                if (!account.getUser().getId().equals(user.getId())) {
                        throw new IllegalArgumentException(
                                        "You are not authorized to access this account");
                }

                return account;
        }

        /**
         * Validate transaction amount.
         */
        private void validateAmount(BigDecimal amount) {

                if (amount == null) {
                        throw new IllegalArgumentException(
                                        "Transaction amount is required");
                }

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException(
                                        "Transaction amount must be greater than zero");
                }

                if (amount.scale() > 2) {
                        throw new IllegalArgumentException(
                                        "Transaction amount cannot have more than 2 decimal places");
                }
        }

        /**
         * Only active accounts can perform transactions.
         */
        private void validateActiveAccount(
                        BankAccount account) {

                if (account.getStatus() != AccountStatus.ACTIVE) {
                        throw new IllegalArgumentException(
                                        "Bank account is not active");
                }
        }

        /**
         * Add fraud information to the transaction description.
         */
        private String buildFraudDescription(
                        String description,
                        FraudAnalysisResponse fraudResult) {

                String baseDescription = description == null ? "" : description.trim();

                String fraudDescription = "Fraud risk: "
                                + fraudResult.getRiskLevel()
                                + " | Score: "
                                + fraudResult.getRiskScore();

                if (baseDescription.isEmpty()) {
                        return fraudDescription;
                }

                return baseDescription + " | " + fraudDescription;
        }

        /**
         * Generate a unique transaction reference.
         */
        private String generateReferenceId() {

                String referenceId;

                do {
                        String timestamp = LocalDateTime.now()
                                        .format(
                                                        DateTimeFormatter
                                                                        .ofPattern("yyyyMMddHHmmss"));

                        String randomPart = UUID.randomUUID()
                                        .toString()
                                        .replace("-", "")
                                        .substring(0, 8)
                                        .toUpperCase();

                        referenceId = "ARTH-TXN-"
                                        + timestamp
                                        + "-"
                                        + randomPart;

                } while (transactionRepository
                                .existsByReferenceId(referenceId));

                return referenceId;
        }
}