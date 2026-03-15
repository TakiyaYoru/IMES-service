package com.imes.common.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record EvaluationComparisonResponse(
        List<EvaluationComparisonItemResponse> items,
        BigDecimal overallAverageScore
) {
}
