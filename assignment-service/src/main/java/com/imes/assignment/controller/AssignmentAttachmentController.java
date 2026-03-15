package com.imes.assignment.controller;

import com.imes.assignment.dto.AttachmentResponse;
import com.imes.assignment.dto.UploadAttachmentResponse;
import com.imes.assignment.service.AssignmentAttachmentService;
import com.imes.common.dto.ResponseApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
@Slf4j
public class AssignmentAttachmentController {

    private final AssignmentAttachmentService assignmentAttachmentService;

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseApi<UploadAttachmentResponse> uploadAttachment(
            @PathVariable("id") Long assignmentId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestHeader(value = "X-User-Id", required = false) Long uploadedBy,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        uploadedBy = requireUserId(uploadedBy);
        requireAnyRole(userRole, "MENTOR", "INTERN", "HR", "ADMIN");
        log.info("Uploading attachment for assignment {} by user {}", assignmentId, uploadedBy);
        UploadAttachmentResponse response = assignmentAttachmentService.uploadAttachment(assignmentId, file, fileType, uploadedBy);
        return ResponseApi.success(response);
    }

        @GetMapping("/{id}/attachments")
        public ResponseApi<List<AttachmentResponse>> getAttachments(
            @PathVariable("id") Long assignmentId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "INTERN", "HR", "ADMIN");
        List<AttachmentResponse> attachments = assignmentAttachmentService.listAttachments(assignmentId);
        return ResponseApi.success(attachments);
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseApi<String> deleteAttachment(
            @PathVariable Long attachmentId,
            @RequestHeader(value = "X-User-Id", required = false) Long requestUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requestUserId = requireUserId(requestUserId);
        requireAnyRole(userRole, "MENTOR", "INTERN", "HR", "ADMIN");
        assignmentAttachmentService.deleteAttachment(attachmentId, requestUserId);
        return ResponseApi.success("Attachment deleted successfully");
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        AssignmentAttachmentService.DownloadFilePayload payload = assignmentAttachmentService.downloadAttachment(attachmentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + payload.fileName() + "\"")
            .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(payload.resource());
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Missing required header: X-User-Id");
        }
        return userId;
    }

    private void requireAnyRole(String role, String... allowedRoles) {
        if (role == null) {
            throw new IllegalArgumentException("Forbidden: missing role");
        }
        for (String allowed : allowedRoles) {
            if (allowed.equalsIgnoreCase(role)) {
                return;
            }
        }
        throw new IllegalArgumentException("Forbidden: role " + role + " is not allowed");
    }
}
