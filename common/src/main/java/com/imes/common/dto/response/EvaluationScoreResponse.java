package com.imes.common.dto.response;

import java.math.BigDecimal;

public record EvaluationScoreResponse(
        Long id,
        Long evaluationId,
        Long criteriaId,
        BigDecimal score,
        String comment,
        String evidence
) {
}
