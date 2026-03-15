package com.imes.attendance.controller;

import com.imes.common.dto.ApiResponse;
import com.imes.common.dto.request.CreateEvaluationTemplateRequest;
import com.imes.common.dto.request.UpdateEvaluationTemplateRequest;
import com.imes.common.dto.response.EvaluationCriteriaResponse;
import com.imes.common.dto.response.EvaluationTemplateResponse;
import com.imes.core.service.EvaluationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evaluations/templates")
@RequiredArgsConstructor
@Tag(name = "Evaluation Template", description = "Quản lý template đánh giá và tiêu chí")
public class EvaluationTemplateController {

    private final EvaluationTemplateService evaluationTemplateService;

    @PostMapping
    @Operation(summary = "Tạo template đánh giá")
    public ResponseEntity<ApiResponse<EvaluationTemplateResponse>> createTemplate(
            @Valid @RequestBody CreateEvaluationTemplateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("Missing required header: X-User-Id");
        }
        requireAnyRole(userRole, "HR", "ADMIN");
        EvaluationTemplateResponse response = evaluationTemplateService.create(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách template", description = "Có thể lọc theo evaluationType")
    public ResponseEntity<ApiResponse<List<EvaluationTemplateResponse>>> getTemplates(
            @RequestParam(required = false) String evaluationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "INTERN", "MENTOR", "HR", "ADMIN");
        List<EvaluationTemplateResponse> response = evaluationTemplateService.getAll(evaluationType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết template")
    public ResponseEntity<ApiResponse<EvaluationTemplateResponse>> getTemplate(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "INTERN", "MENTOR", "HR", "ADMIN");
        EvaluationTemplateResponse response = evaluationTemplateService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật template")
    public ResponseEntity<ApiResponse<EvaluationTemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEvaluationTemplateRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "HR", "ADMIN");
        EvaluationTemplateResponse response = evaluationTemplateService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm template", description = "Đặt trạng thái template về inactive")
    public ResponseEntity<ApiResponse<String>> deleteTemplate(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "HR", "ADMIN");
        evaluationTemplateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted successfully"));
    }

    @GetMapping("/{id}/criteria")
    @Operation(summary = "Lấy danh sách tiêu chí theo template")
    public ResponseEntity<ApiResponse<List<EvaluationCriteriaResponse>>> getCriteria(
            @PathVariable("id") Long templateId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "INTERN", "MENTOR", "HR", "ADMIN");
        List<EvaluationCriteriaResponse> response = evaluationTemplateService.getCriteriaByTemplate(templateId);
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
