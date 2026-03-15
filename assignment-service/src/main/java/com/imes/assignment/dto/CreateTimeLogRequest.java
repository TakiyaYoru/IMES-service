package com.imes.assignment.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTimeLogRequest(
        @NotNull(message = "Hours spent is required")
        @DecimalMin(value = "0.25", message = "Hours spent must be at least 0.25")
        @DecimalMax(value = "24.00", message = "Hours spent must not exceed 24")
        @Digits(integer = 3, fraction = 2, message = "Hours spent must have up to 2 decimal places")
        BigDecimal hoursSpent,

        @NotNull(message = "Work date is required")
        LocalDate workDate,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description
) {
}
