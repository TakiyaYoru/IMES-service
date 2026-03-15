package com.imes.assignment.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AttachmentResponse(
        Long id,
        Long assignmentId,
        String fileName,
        String fileType,
        Long fileSize,
        Long uploadedBy,
        LocalDateTime uploadedAt,
        String downloadUrl
) {
}
