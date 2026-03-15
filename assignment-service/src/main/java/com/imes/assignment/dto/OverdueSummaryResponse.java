package com.imes.assignment.dto;

import lombok.Builder;

@Builder
public record OverdueSummaryResponse(
        long totalAssignments,
        long overdueAssignments,
        double overduePercentage
) {
}
