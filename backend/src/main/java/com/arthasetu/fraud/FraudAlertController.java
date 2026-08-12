package com.arthasetu.fraud;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fraud/alerts")
@RequiredArgsConstructor
public class FraudAlertController {

    private final FraudAlertService fraudAlertService;

    /**
     * Get all fraud alerts.
     */
    @GetMapping
    public ResponseEntity<List<FraudAlertResponse>> getAllAlerts() {

        return ResponseEntity.ok(
                fraudAlertService.getAllAlerts());
    }

    /**
     * Get fraud alerts by status.
     *
     * Example:
     * GET /api/fraud/alerts/status/NEW
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<FraudAlertResponse>> getAlertsByStatus(
            @PathVariable FraudAlertStatus status) {

        return ResponseEntity.ok(
                fraudAlertService.getAlertsByStatus(status));
    }

    /**
     * Get one fraud alert by ID.
     *
     * Example:
     * GET /api/fraud/alerts/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<FraudAlertResponse> getAlert(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                fraudAlertService.getAlert(id));
    }

    /**
     * Mark a fraud alert as reviewed.
     *
     * Example:
     * PATCH /api/fraud/alerts/1/review
     */
    @PatchMapping("/{id}/review")
    public ResponseEntity<FraudAlertResponse> markAsReviewed(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                fraudAlertService.markAsReviewed(id));
    }

    /**
     * Mark a fraud alert as resolved.
     *
     * Example:
     * PATCH /api/fraud/alerts/1/resolve
     */
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<FraudAlertResponse> markAsResolved(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                fraudAlertService.markAsResolved(id));
    }
}