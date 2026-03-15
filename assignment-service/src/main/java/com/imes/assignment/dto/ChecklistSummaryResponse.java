package com.imes.assignment.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ChecklistSummaryResponse(
        List<ChecklistItemResponse> items,
        long totalItems,
        long completedItems,
        double completionPercentage
) {
}
