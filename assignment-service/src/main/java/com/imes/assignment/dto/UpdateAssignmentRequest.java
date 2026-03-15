package com.imes.assignment.dto;

import com.imes.assignment.entity.AssignmentStatus;
import jakarta.validation.constraints.Future;
import java.time.LocalDate;

public record UpdateAssignmentRequest(
        String title,
        String description,
        @Future(message = "Deadline must be in the future")
        LocalDate deadline,
        AssignmentStatus status
) {}
