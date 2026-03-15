package com.imes.common.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EvaluationScoreInputRequest(
        @NotNull(message = "criteriaId is required")
        Long criteriaId,
        @NotNull(message = "score is required")
        @DecimalMin(value = "0.0", message = "score must be >= 0")
        BigDecimal score,
        String comment,
        String evidence
) {
}
