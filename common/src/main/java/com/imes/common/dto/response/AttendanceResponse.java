package com.imes.common.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AttendanceResponse(
    Long id,
    Long internProfileId,
    LocalDate date,
    LocalTime checkInTime,
    LocalTime checkOutTime,
    String status,
    BigDecimal totalHours,
    String notes,
    Long approvedBy,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
