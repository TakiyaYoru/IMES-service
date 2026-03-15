package com.imes.assignment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ReviewSubmissionRequest(
    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.0", message = "Score must be between 0 and 10")
    @DecimalMax(value = "10.0", message = "Score must be between 0 and 10")
    Double score,
    
    String comments
) {}
