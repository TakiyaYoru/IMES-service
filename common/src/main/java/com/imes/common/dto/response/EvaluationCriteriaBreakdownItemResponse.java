package com.imes.common.dto.response;

import java.math.BigDecimal;

public record EvaluationCriteriaBreakdownItemResponse(
        Long criteriaId,
        String criteriaName,
        Integer maxScore,
        long evaluationsCount,
        BigDecimal averageScore
) {
}
