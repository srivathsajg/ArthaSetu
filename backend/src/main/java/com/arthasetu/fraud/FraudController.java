package com.arthasetu.fraud;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudDetectionService fraudDetectionService;

    @PostMapping("/analyze")
    public ResponseEntity<FraudAnalysisResponse> analyze(
            @Valid @RequestBody FraudAnalysisRequest request) {

        FraudAnalysisResponse response = fraudDetectionService.analyze(request);

        return ResponseEntity.ok(response);
    }
}