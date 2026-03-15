package com.imes.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateEvaluationRequest(
        @NotNull(message = "internProfileId is required")
        Long internProfileId,
        @NotNull(message = "templateId is required")
        Long templateId,
        @NotBlank(message = "evaluationType is required")
        String evaluationType,
        @NotNull(message = "periodStart is required")
        LocalDate periodStart,
        @NotNull(message = "periodEnd is required")
        LocalDate periodEnd,
        String overallComment
) {
}
