package com.imes.common.dto.request;

import java.time.LocalDateTime;

/**
 * Request DTO for updating an assignment
 */
public record UpdateAssignmentRequest(
    String title,
    String description,
    LocalDateTime deadline
) {}
