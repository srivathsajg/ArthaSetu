package com.arthasetu.fraud;

import com.arthasetu.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FraudAlertRepository
        extends JpaRepository<FraudAlert, Long> {

    /**
     * Find the fraud alert associated with a transaction.
     */
    java.util.Optional<FraudAlert> findByTransaction(
            Transaction transaction);

    /**
     * Get all fraud alerts ordered by newest first.
     */
    List<FraudAlert> findAllByOrderByCreatedAtDesc();

    /**
     * Get fraud alerts by status.
     */
    List<FraudAlert> findByStatusOrderByCreatedAtDesc(
            FraudAlertStatus status);

    /**
     * Check whether a transaction already has a fraud alert.
     */
    boolean existsByTransaction(Transaction transaction);
}