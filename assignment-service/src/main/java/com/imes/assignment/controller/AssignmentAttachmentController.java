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
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "3") Long uploadedBy
    ) {
        log.info("Uploading attachment for assignment {} by user {}", assignmentId, uploadedBy);
        UploadAttachmentResponse response = assignmentAttachmentService.uploadAttachment(assignmentId, file, fileType, uploadedBy);
        return ResponseApi.success(response);
    }

    @GetMapping("/{id}/attachments")
    public ResponseApi<List<AttachmentResponse>> getAttachments(@PathVariable("id") Long assignmentId) {
        List<AttachmentResponse> attachments = assignmentAttachmentService.listAttachments(assignmentId);
        return ResponseApi.success(attachments);
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseApi<String> deleteAttachment(
            @PathVariable Long attachmentId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "3") Long requestUserId
    ) {
        assignmentAttachmentService.deleteAttachment(attachmentId, requestUserId);
        return ResponseApi.success("Attachment deleted successfully");
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        AssignmentAttachmentService.DownloadFilePayload payload = assignmentAttachmentService.downloadAttachment(attachmentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + payload.fileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(payload.resource());
    }
}
