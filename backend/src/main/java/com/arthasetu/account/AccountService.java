package com.arthasetu.account;

import com.arthasetu.user.User;
import com.arthasetu.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Create a new bank account for the authenticated user.
     */
    @Transactional
    public BankAccount createAccount(
            String email,
            AccountType accountType) {

        // Find the authenticated user.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate a unique account number.
        String accountNumber = generateUniqueAccountNumber();

        // Create the bank account.
        BankAccount account = BankAccount.builder()
                .accountNumber(accountNumber)
                .accountType(accountType)
                .balance(BigDecimal.ZERO)
                .ifscCode("ARTH0000001")
                .status(AccountStatus.ACTIVE)
                .user(user)
                .build();

        return bankAccountRepository.save(account);
    }

    /**
     * Get all bank accounts belonging to the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<BankAccount> getMyAccounts(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return bankAccountRepository.findByUser(user);
    }

    /**
     * Find one account belonging to the authenticated user.
     */
    @Transactional(readOnly = true)
    public BankAccount getMyAccount(
            String email,
            String accountNumber) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BankAccount account = bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Bank account not found"));

        // Prevent one customer from accessing another customer's account.
        if (!account.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "You are not authorized to access this account");
        }

        return account;
    }

    /**
     * Generate a 16-digit unique bank account number.
     */
    private String generateUniqueAccountNumber() {

        String accountNumber;

        do {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < 16; i++) {
                int digit = secureRandom.nextInt(10);

                // First digit should not be zero.
                if (i == 0 && digit == 0) {
                    digit = 1 + secureRandom.nextInt(9);
                }

                builder.append(digit);
            }

            accountNumber = builder.toString();

        } while (bankAccountRepository
                .existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}