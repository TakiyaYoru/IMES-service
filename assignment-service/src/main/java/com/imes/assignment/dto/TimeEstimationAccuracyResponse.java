package com.imes.assignment.dto;

import lombok.Builder;

@Builder
public record TimeEstimationAccuracyResponse(
        long totalCompletedAssignments,
        long onTimeCompletedAssignments,
        long lateCompletedAssignments,
        double accuracyPercentage
) {
}
