package com.imes.assignment.dto;

import com.imes.assignment.entity.AssignmentStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BulkUpdateAssignmentStatusRequest(
        @NotEmpty(message = "assignmentIds must not be empty")
        List<Long> assignmentIds,

        @NotNull(message = "targetStatus is required")
        AssignmentStatus targetStatus
) {
}
