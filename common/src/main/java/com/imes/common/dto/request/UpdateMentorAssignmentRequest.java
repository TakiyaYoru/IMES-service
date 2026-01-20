package com.imes.common.dto.request;

import java.time.LocalDate;

public record UpdateMentorAssignmentRequest(
    LocalDate startDate,
    LocalDate endDate,
    String assignmentStatus
) {}
