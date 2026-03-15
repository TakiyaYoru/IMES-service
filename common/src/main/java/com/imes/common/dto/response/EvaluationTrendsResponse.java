package com.imes.common.dto.response;

import java.util.List;

public record EvaluationTrendsResponse(
        String granularity,
        List<EvaluationTrendPointResponse> points
) {
}
