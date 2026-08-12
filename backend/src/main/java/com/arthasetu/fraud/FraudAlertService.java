package com.arthasetu.fraud;

import com.arthasetu.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudAlertService {

    private final FraudAlertRepository fraudAlertRepository;

    /**
     * Create a fraud alert for a suspicious transaction.
     */
    @Transactional
    public FraudAlert createAlert(
            Transaction transaction,
            FraudAnalysisResponse fraudResult) {

        // Do not create duplicate alerts for the same transaction.
        if (fraudAlertRepository.existsByTransaction(transaction)) {
            return fraudAlertRepository
                    .findByTransaction(transaction)
                    .orElseThrow(() -> new IllegalStateException(
                            "Fraud alert already exists"));
        }

        FraudAlert alert = FraudAlert.builder()
                .transaction(transaction)
                .riskScore(fraudResult.getRiskScore())
                .riskLevel(fraudResult.getRiskLevel())
                .reason(fraudResult.getReason())
                .amount(transaction.getAmount())
                .status(FraudAlertStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();

        return fraudAlertRepository.save(alert);
    }

    /**
     * Get all fraud alerts.
     *
     * The conversion to FraudAlertResponse happens while the
     * Hibernate session is still active.
     */
    @Transactional(readOnly = true)
    public List<FraudAlertResponse> getAllAlerts() {

        return fraudAlertRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get fraud alerts by status.
     */
    @Transactional(readOnly = true)
    public List<FraudAlertResponse> getAlertsByStatus(
            FraudAlertStatus status) {

        return fraudAlertRepository
                .findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get one fraud alert by ID.
     */
    @Transactional(readOnly = true)
    public FraudAlertResponse getAlert(Long id) {

        FraudAlert alert = fraudAlertRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fraud alert not found"));

        return toResponse(alert);
    }

    /**
     * Mark a fraud alert as reviewed.
     */
    @Transactional
    public FraudAlertResponse markAsReviewed(Long id) {

        FraudAlert alert = fraudAlertRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fraud alert not found"));

        alert.setStatus(FraudAlertStatus.REVIEWED);
        alert.setUpdatedAt(LocalDateTime.now());

        FraudAlert savedAlert = fraudAlertRepository.save(alert);

        return toResponse(savedAlert);
    }

    /**
     * Mark a fraud alert as resolved.
     */
    @Transactional
    public FraudAlertResponse markAsResolved(Long id) {

        FraudAlert alert = fraudAlertRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fraud alert not found"));

        alert.setStatus(FraudAlertStatus.RESOLVED);
        alert.setUpdatedAt(LocalDateTime.now());

        FraudAlert savedAlert = fraudAlertRepository.save(alert);

        return toResponse(savedAlert);
    }

    /**
     * Convert FraudAlert entity into API response.
     *
     * This method is called inside an active transaction,
     * so the LAZY transaction relationship can safely be accessed.
     */
    private FraudAlertResponse toResponse(FraudAlert alert) {

        Transaction transaction = alert.getTransaction();

        return new FraudAlertResponse(
                alert.getId(),
                transaction.getId(),
                transaction.getReferenceId(),
                alert.getAmount(),
                alert.getRiskScore(),
                alert.getRiskLevel(),
                alert.getReason(),
                alert.getStatus(),
                alert.getCreatedAt(),
                alert.getUpdatedAt());
    }
}