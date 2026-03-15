package com.imes.common.dto.response;

public record AttendanceMonthlySummaryResponse(
        Integer year,
        Integer month,
        Integer totalRecords,
        Integer presentCount,
        Integer lateCount,
        Integer absentCount,
        Integer leaveCount,
        Integer halfDayCount,
        Double attendanceRate
) {
}
