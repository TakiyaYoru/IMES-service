package com.imes.attendance.controller;

import com.imes.common.dto.ApiResponse;
import com.imes.common.dto.request.CreateEvaluationRequest;
import com.imes.common.dto.request.UpdateEvaluationRequest;
import com.imes.common.dto.response.EvaluationComparisonResponse;
import com.imes.common.dto.response.EvaluationCriteriaBreakdownResponse;
import com.imes.common.dto.response.EvaluationResponse;
import com.imes.common.dto.response.EvaluationTrendsResponse;
import com.imes.core.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Evaluation", description = "Quản lý đánh giá thực tập sinh")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping
        @Operation(summary = "Tạo evaluation mới", description = "Tạo bản đánh giá ở trạng thái DRAFT")
    public ResponseEntity<ApiResponse<EvaluationResponse>> createEvaluation(
            @Valid @RequestBody CreateEvaluationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long evaluatorId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        if (evaluatorId == null) {
            throw new IllegalArgumentException("Missing required header: X-User-Id");
        }
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        EvaluationResponse response = evaluationService.create(request, evaluatorId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách evaluations", description = "Hỗ trợ filter theo intern, evaluator, status, evaluationType")
    public ResponseEntity<ApiResponse<List<EvaluationResponse>>> getEvaluations(
            @RequestParam(required = false) Long internId,
            @RequestParam(required = false) Long evaluatorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String evaluationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "INTERN", "MENTOR", "HR", "ADMIN");
        List<EvaluationResponse> response = evaluationService.getAll(internId, evaluatorId, status, evaluationType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết evaluation")
    public ResponseEntity<ApiResponse<EvaluationResponse>> getEvaluation(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "INTERN", "MENTOR", "HR", "ADMIN");
        EvaluationResponse response = evaluationService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
        @Operation(summary = "Cập nhật evaluation DRAFT", description = "Cập nhật comment và score theo tiêu chí")
    public ResponseEntity<ApiResponse<EvaluationResponse>> updateEvaluation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEvaluationRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        EvaluationResponse response = evaluationService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit evaluation", description = "Chuyển trạng thái DRAFT -> SUBMITTED")
    public ResponseEntity<ApiResponse<EvaluationResponse>> submitEvaluation(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        EvaluationResponse response = evaluationService.submit(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "Review evaluation", description = "Chuyển trạng thái SUBMITTED -> REVIEWED")
    public ResponseEntity<ApiResponse<EvaluationResponse>> reviewEvaluation(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        EvaluationResponse response = evaluationService.review(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/finalize")
    @Operation(summary = "Finalize evaluation", description = "Chuyển trạng thái REVIEWED -> FINALIZED")
    public ResponseEntity<ApiResponse<EvaluationResponse>> finalizeEvaluation(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "HR", "ADMIN");
        EvaluationResponse response = evaluationService.finalizeEvaluation(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics/comparison")
        @Operation(summary = "Analytics: comparison", description = "So sánh điểm trung bình và điểm mới nhất theo intern")
    public ResponseEntity<ApiResponse<EvaluationComparisonResponse>> getComparison(
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        EvaluationComparisonResponse response = evaluationService.getComparison(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics/trends")
        @Operation(summary = "Analytics: trends", description = "Xu hướng điểm theo DAILY hoặc MONTHLY")
    public ResponseEntity<ApiResponse<EvaluationTrendsResponse>> getTrends(
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "DAILY hoặc MONTHLY")
            @RequestParam(defaultValue = "MONTHLY") String granularity,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        EvaluationTrendsResponse response = evaluationService.getTrends(startDate, endDate, granularity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics/criteria-breakdown")
        @Operation(summary = "Analytics: criteria breakdown", description = "Phân tích điểm trung bình theo từng tiêu chí")
    public ResponseEntity<ApiResponse<EvaluationCriteriaBreakdownResponse>> getCriteriaBreakdown(
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        EvaluationCriteriaBreakdownResponse response = evaluationService.getCriteriaBreakdown(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private void requireAnyRole(String role, String... allowedRoles) {
        if (role == null) {
            throw new IllegalArgumentException("Forbidden: missing role");
        }
        for (String allowed : allowedRoles) {
            if (allowed.equalsIgnoreCase(role)) {
                return;
            }
        }
        throw new IllegalArgumentException("Forbidden: role " + role + " is not allowed");
    }
}
