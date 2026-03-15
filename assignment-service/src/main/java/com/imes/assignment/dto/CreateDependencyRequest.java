package com.imes.assignment.dto;

import jakarta.validation.constraints.NotNull;

public record CreateDependencyRequest(
        @NotNull(message = "dependsOnAssignmentId is required")
        Long dependsOnAssignmentId
) {
}
