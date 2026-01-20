package com.imes.common.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MentorAssignmentResponse(
    Long id,
    Long mentorId,
    Long internProfileId,
    String assignmentStatus,
    LocalDate startDate,
    LocalDate endDate,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
