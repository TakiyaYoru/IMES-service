package com.imes.assignment.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChecklistItemResponse(
        Long id,
        Long assignmentId,
        String itemText,
        boolean isCompleted,
        Integer displayOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
