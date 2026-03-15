package com.imes.assignment.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitAssignmentRequest(
    @NotBlank(message = "Content is required")
    String content,
    
    String attachmentUrl
) {}
