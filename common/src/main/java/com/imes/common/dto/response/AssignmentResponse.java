package com.imes.common.dto.response;

import java.time.LocalDateTime;

/**
 * Response DTO for Assignment
 */
public record AssignmentResponse(
    Long id,
    String title,
    String description,
    LocalDateTime deadline,
    Long createdBy,
    String mentorName, // Will be populated from User service
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
