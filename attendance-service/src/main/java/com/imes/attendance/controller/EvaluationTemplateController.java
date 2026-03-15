package com.imes.attendance.controller;

import com.imes.common.dto.ApiResponse;
import com.imes.common.dto.request.CreateEvaluationTemplateRequest;
import com.imes.common.dto.request.UpdateEvaluationTemplateRequest;
import com.imes.common.dto.response.EvaluationCriteriaResponse;
import com.imes.common.dto.response.EvaluationTemplateResponse;
import com.imes.core.service.EvaluationTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evaluations/templates")
@RequiredArgsConstructor
public class EvaluationTemplateController {

    private final EvaluationTemplateService evaluationTemplateService;

    @PostMapping
    public ResponseEntity<ApiResponse<EvaluationTemplateResponse>> createTemplate(
            @Valid @RequestBody CreateEvaluationTemplateRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId
    ) {
        EvaluationTemplateResponse response = evaluationTemplateService.create(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EvaluationTemplateResponse>>> getTemplates(
            @RequestParam(required = false) String evaluationType
    ) {
        List<EvaluationTemplateResponse> response = evaluationTemplateService.getAll(evaluationType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluationTemplateResponse>> getTemplate(@PathVariable Long id) {
        EvaluationTemplateResponse response = evaluationTemplateService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluationTemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEvaluationTemplateRequest request
    ) {
        EvaluationTemplateResponse response = evaluationTemplateService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTemplate(@PathVariable Long id) {
        evaluationTemplateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted successfully"));
    }

    @GetMapping("/{id}/criteria")
    public ResponseEntity<ApiResponse<List<EvaluationCriteriaResponse>>> getCriteria(@PathVariable("id") Long templateId) {
        List<EvaluationCriteriaResponse> response = evaluationTemplateService.getCriteriaByTemplate(templateId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
