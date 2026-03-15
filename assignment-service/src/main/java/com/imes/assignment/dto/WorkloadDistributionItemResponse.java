package com.imes.assignment.dto;

import lombok.Builder;

@Builder
public record WorkloadDistributionItemResponse(
        Long mentorId,
        long totalAssignments,
        long activeAssignments,
        long completedAssignments,
        long overdueAssignments
) {
}
