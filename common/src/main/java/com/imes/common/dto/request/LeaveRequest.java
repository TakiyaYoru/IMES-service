package com.imes.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeaveRequest(
    @NotNull(message = "Intern profile ID is required")
    Long internProfileId,

    @NotNull(message = "Leave date is required")
    LocalDate leaveDate,

    @NotBlank(message = "Reason is required")
    String reason
) {}
