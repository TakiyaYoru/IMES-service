package com.imes.common.dto.response;

import java.math.BigDecimal;

public record AttendanceStatisticsResponse(
    Long internProfileId,
    Integer totalDays,
    Integer presentDays,
    Integer lateDays,
    Integer absentDays,
    Integer leaveDays,
    BigDecimal totalHours,
    Double attendanceRate
) {}
