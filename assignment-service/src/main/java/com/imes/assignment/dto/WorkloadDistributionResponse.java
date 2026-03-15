package com.imes.assignment.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record WorkloadDistributionResponse(
        List<WorkloadDistributionItemResponse> items,
        long totalMentors,
        long totalActiveAssignments
) {
}
