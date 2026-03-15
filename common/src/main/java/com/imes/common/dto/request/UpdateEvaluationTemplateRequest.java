package com.imes.common.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateEvaluationTemplateRequest(
        @NotBlank(message = "Template name is required")
        String name,
        String description,
        @NotBlank(message = "Evaluation type is required")
        String evaluationType,
        Boolean isActive
) {
}
