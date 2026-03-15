package com.imes.common.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Aggregated summary of an intern for HR decision support.
 * Combines profile + attendance stats + latest evaluation score.
 */
public record InternSummaryResponse(
        Long internId,
        String fullName,
        String email,
        String studentId,
        String major,
        String university,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        Long mentorId,
        Long departmentId,
        long totalAttendanceDays,
        long presentDays,
        long lateDays,
        long absentDays,
        long leaveDays,
        double attendanceRate,
        double totalHoursWorked,
        Long latestEvaluationId,
        String latestEvaluationType,
        BigDecimal latestFinalScore,
        String latestEvaluationStatus
) {}
