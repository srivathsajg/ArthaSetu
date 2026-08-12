package com.arthasetu.transaction;

import com.arthasetu.account.BankAccount;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_reference", columnList = "reference_id"),
        @Index(name = "idx_transaction_source", columnList = "source_account_id"),
        @Index(name = "idx_transaction_destination", columnList = "destination_account_id"),
        @Index(name = "idx_transaction_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique reference number for the transaction.
     */
    @Column(name = "reference_id", nullable = false, unique = true, length = 40)
    private String referenceId;

    /**
     * Amount involved in the transaction.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Type of transaction.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    /**
     * Current state of the transaction.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    /**
     * Fraud risk score calculated for this transaction.
     * Range: 0-100.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer riskScore = 0;

    /**
     * Fraud risk level calculated for this transaction.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FraudRiskLevel riskLevel;

    /**
     * Account from which money is taken.
     *
     * For DEPOSIT transactions this can be null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", foreignKey = @ForeignKey(name = "fk_transaction_source_account"))
    private BankAccount sourceAccount;

    /**
     * Account to which money is added.
     *
     * For WITHDRAWAL transactions this can be null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id", foreignKey = @ForeignKey(name = "fk_transaction_destination_account"))
    private BankAccount destinationAccount;

    /**
     * Optional description provided for the transaction.
     */
    @Column(length = 255)
    private String description;

    /**
     * Transaction creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}