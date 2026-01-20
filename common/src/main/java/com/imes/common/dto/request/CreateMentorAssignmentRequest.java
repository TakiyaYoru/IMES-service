package com.imes.common.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;

public record CreateMentorAssignmentRequest(
    @NotNull(message = "Mentor ID is required")
    Long mentorId,

    @NotNull(message = "Intern profile ID is required")
    Long internProfileId,

    @FutureOrPresent(message = "Start date must be in the future or today")
    LocalDate startDate,

    LocalDate endDate
) {}
