package com.imes.common.dto.response;

import java.math.BigDecimal;

public record TopPerformerResponse(
        Long internProfileId,
        Double attendanceRate,
        Integer presentDays,
        Integer lateDays,
        Integer absentDays,
        BigDecimal totalHours
) {
}
