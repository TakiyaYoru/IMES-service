package com.imes.common.dto.response;

import java.time.LocalDateTime;

public record EvaluationTemplateResponse(
        Long id,
        String name,
        String description,
        String evaluationType,
        Boolean isActive,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
