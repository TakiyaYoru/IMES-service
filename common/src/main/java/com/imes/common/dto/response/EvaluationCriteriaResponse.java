package com.imes.common.dto.response;

import java.math.BigDecimal;

public record EvaluationCriteriaResponse(
        Long id,
        Long templateId,
        String category,
        String criteriaName,
        String criteriaDescription,
        BigDecimal weight,
        Integer maxScore,
        Integer displayOrder
) {
}
