package com.imes.assignment.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record TimeLogResponse(
        Long id,
        Long assignmentId,
        Long internId,
        BigDecimal hoursSpent,
        LocalDate workDate,
        String description,
        LocalDateTime createdAt
) {
}
