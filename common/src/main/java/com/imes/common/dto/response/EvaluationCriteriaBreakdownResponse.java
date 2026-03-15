package com.imes.common.dto.response;

import java.util.List;

public record EvaluationCriteriaBreakdownResponse(
        List<EvaluationCriteriaBreakdownItemResponse> items
) {
}
