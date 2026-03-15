package com.imes.common.dto.response;

import java.math.BigDecimal;

public record EvaluationComparisonItemResponse(
        Long internProfileId,
        long evaluationsCount,
        BigDecimal averageScore,
        BigDecimal latestScore
) {
}
