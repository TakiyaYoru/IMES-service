package com.imes.common.dto.response;

import java.math.BigDecimal;

public record EvaluationTrendPointResponse(
        String label,
        long evaluationsCount,
        BigDecimal averageScore
) {
}
