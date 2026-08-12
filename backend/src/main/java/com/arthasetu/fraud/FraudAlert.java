package com.arthasetu.fraud;

import com.arthasetu.transaction.Transaction;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts", indexes = {
        @Index(name = "idx_fraud_alert_transaction", columnList = "transaction_id"),
        @Index(name = "idx_fraud_alert_status", columnList = "status"),
        @Index(name = "idx_fraud_alert_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Transaction that triggered this fraud alert.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_fraud_alert_transaction"))
    private Transaction transaction;

    /**
     * Fraud risk score calculated by the fraud engine.
     */
    @Column(nullable = false)
    private Integer riskScore;

    /**
     * Fraud risk level.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel;

    /**
     * Reason why the transaction was considered suspicious.
     */
    @Column(nullable = false, length = 1000)
    private String reason;

    /**
     * Current status of the fraud alert.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FraudAlertStatus status = FraudAlertStatus.NEW;

    /**
     * Amount involved in the suspicious transaction.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Time when the fraud alert was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Time when the alert was last reviewed or updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}