package com.imes.common.dto.response;

public record AttendanceTrendPointResponse(
        String label,
        Integer total,
        Integer present,
        Integer late,
        Integer absent,
        Integer leave,
        Integer halfDay
) {
}
