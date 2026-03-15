package com.imes.common.dto.request;

import jakarta.validation.Valid;

import java.util.List;

public record UpdateEvaluationRequest(
        String overallComment,
        @Valid
        List<EvaluationScoreInputRequest> scores
) {
}
