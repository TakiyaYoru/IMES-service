package com.imes.assignment.dto;

import lombok.Builder;

@Builder
public record UploadAttachmentResponse(
        AttachmentResponse attachment,
        String message
) {
}
