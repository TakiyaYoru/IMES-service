package com.imes.common.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceAnomalyResponse(
        Long id,
        Long internProfileId,
        LocalDate anomalyDate,
        String anomalyType,
        String severity,
        String description,
        Boolean resolved,
        LocalDateTime createdAt
) {
}
