package com.imes.common.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record EvaluationResponse(
        Long id,
        Long internProfileId,
        Long templateId,
        Long evaluatorId,
        String evaluationType,
        LocalDate periodStart,
        LocalDate periodEnd,
        String status,
        BigDecimal totalScore,
        String grade,
        String overallComment,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        LocalDateTime finalizedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<EvaluationScoreResponse> scores
) {
}
