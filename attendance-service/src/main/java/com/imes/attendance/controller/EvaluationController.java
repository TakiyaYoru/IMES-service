package com.imes.attendance.controller;

import com.imes.common.dto.ApiResponse;
import com.imes.common.dto.request.CreateEvaluationRequest;
import com.imes.common.dto.request.UpdateEvaluationRequest;
import com.imes.common.dto.response.EvaluationComparisonResponse;
import com.imes.common.dto.response.EvaluationCriteriaBreakdownResponse;
import com.imes.common.dto.response.EvaluationResponse;
import com.imes.common.dto.response.EvaluationTrendsResponse;
import com.imes.core.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<ApiResponse<EvaluationResponse>> createEvaluation(
            @Valid @RequestBody CreateEvaluationRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "2") Long evaluatorId
    ) {
        EvaluationResponse response = evaluationService.create(request, evaluatorId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EvaluationResponse>>> getEvaluations(
            @RequestParam(required = false) Long internId,
            @RequestParam(required = false) Long evaluatorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String evaluationType
    ) {
        List<EvaluationResponse> response = evaluationService.getAll(internId, evaluatorId, status, evaluationType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluationResponse>> getEvaluation(@PathVariable Long id) {
        EvaluationResponse response = evaluationService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluationResponse>> updateEvaluation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEvaluationRequest request
    ) {
        EvaluationResponse response = evaluationService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<EvaluationResponse>> submitEvaluation(@PathVariable Long id) {
        EvaluationResponse response = evaluationService.submit(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<ApiResponse<EvaluationResponse>> reviewEvaluation(@PathVariable Long id) {
        EvaluationResponse response = evaluationService.review(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/finalize")
    public ResponseEntity<ApiResponse<EvaluationResponse>> finalizeEvaluation(@PathVariable Long id) {
        EvaluationResponse response = evaluationService.finalizeEvaluation(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics/comparison")
    public ResponseEntity<ApiResponse<EvaluationComparisonResponse>> getComparison(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        EvaluationComparisonResponse response = evaluationService.getComparison(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics/trends")
    public ResponseEntity<ApiResponse<EvaluationTrendsResponse>> getTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "MONTHLY") String granularity
    ) {
        EvaluationTrendsResponse response = evaluationService.getTrends(startDate, endDate, granularity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics/criteria-breakdown")
    public ResponseEntity<ApiResponse<EvaluationCriteriaBreakdownResponse>> getCriteriaBreakdown(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        EvaluationCriteriaBreakdownResponse response = evaluationService.getCriteriaBreakdown(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
