package com.imes.assignment.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AssignmentCommentResponse(
        Long id,
        Long assignmentId,
        Long userId,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean edited
) {
}
