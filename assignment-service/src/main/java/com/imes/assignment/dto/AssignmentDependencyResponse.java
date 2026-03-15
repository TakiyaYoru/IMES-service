package com.imes.assignment.dto;

import com.imes.assignment.entity.AssignmentStatus;
import lombok.Builder;

@Builder
public record AssignmentDependencyResponse(
        Long assignmentId,
        Long dependsOnAssignmentId,
        AssignmentStatus dependsOnStatus
) {
}
