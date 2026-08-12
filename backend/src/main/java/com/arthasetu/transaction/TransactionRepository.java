package com.arthasetu.transaction;

import com.arthasetu.account.BankAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByReferenceId(String referenceId);

    boolean existsByReferenceId(String referenceId);

    @EntityGraph(attributePaths = { "sourceAccount", "destinationAccount" })
    List<Transaction> findBySourceAccountOrderByCreatedAtDesc(
            BankAccount sourceAccount);

    @EntityGraph(attributePaths = { "sourceAccount", "destinationAccount" })
    List<Transaction> findByDestinationAccountOrderByCreatedAtDesc(
            BankAccount destinationAccount);
}