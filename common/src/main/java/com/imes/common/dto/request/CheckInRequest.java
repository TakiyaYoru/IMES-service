package com.imes.common.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.time.LocalTime;

public record CheckInRequest(
    @NotNull(message = "Intern profile ID is required")
    Long internProfileId,

    @PastOrPresent(message = "Date must be today or in the past")
    LocalDate date,

    LocalTime checkInTime
) {}
