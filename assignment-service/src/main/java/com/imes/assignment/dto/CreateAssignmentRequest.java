package com.imes.assignment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateAssignmentRequest(
    @NotBlank(message = "Title is required")
    String title,
    
    String description,
    
    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    LocalDate deadline
) {}
