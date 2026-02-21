package com.imes.common.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InternProfileResponse(
        Long id,
        String email,
        String studentId,
        String fullName,
        String phoneNumber,
        String major,
        String university,
        BigDecimal gpa,
        String skills,
        LocalDate startDate,
        LocalDate endDate,
        Long mentorId,
        String mentorName,
        Long departmentId,
        String departmentName,
        String avatarUrl,
        String status,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
