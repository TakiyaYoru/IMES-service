package com.imes.common.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CheckOutRequest(
    @NotNull(message = "Attendance ID is required")
    Long attendanceId,

    LocalTime checkOutTime
) {}
