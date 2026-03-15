package com.imes.assignment.dto;

import lombok.Builder;

@Builder
public record CompletionRateResponse(
        long totalAssignments,
        long completedAssignments,
        double completionRate
) {
}
