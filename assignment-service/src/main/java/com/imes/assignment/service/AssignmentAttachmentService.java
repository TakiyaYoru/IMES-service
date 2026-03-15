package com.imes.assignment.service;

import com.imes.assignment.dto.AttachmentResponse;
import com.imes.assignment.dto.UploadAttachmentResponse;
import com.imes.assignment.entity.Assignment;
import com.imes.assignment.entity.AssignmentAttachment;
import com.imes.assignment.repository.AssignmentAttachmentRepository;
import com.imes.assignment.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentAttachmentService {

    private final AssignmentAttachmentRepository assignmentAttachmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final LocalFileStorageService localFileStorageService;

    @Transactional
    public UploadAttachmentResponse uploadAttachment(Long assignmentId, MultipartFile file, String fileType, Long uploadedBy) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        LocalFileStorageService.StoredFileInfo stored = localFileStorageService.uploadFile(file, "assignments");

        AssignmentAttachment attachment = AssignmentAttachment.builder()
                .assignmentId(assignment.getId())
                .fileName(stored.originalFileName())
                .storagePath(stored.storedPath())
                .fileType(fileType != null && !fileType.isBlank() ? fileType : stored.fileType())
                .fileSize(stored.fileSize())
                .uploadedBy(uploadedBy)
                .build();

        AssignmentAttachment saved = assignmentAttachmentRepository.save(attachment);

        return UploadAttachmentResponse.builder()
                .attachment(toResponse(saved))
                .message("Attachment uploaded successfully")
                .build();
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> listAttachments(Long assignmentId) {
        return assignmentAttachmentRepository.findByAssignmentIdAndIsActiveTrueOrderByUploadedAtDesc(assignmentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DownloadFilePayload downloadAttachment(Long attachmentId) {
        AssignmentAttachment attachment = assignmentAttachmentRepository.findByIdAndIsActiveTrue(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found: " + attachmentId));

        Path filePath = localFileStorageService.resolveDownloadPath(attachment.getStoragePath());
        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid download URL", e);
        }

        return new DownloadFilePayload(resource, attachment.getFileName());
    }

    @Transactional
    public void deleteAttachment(Long attachmentId, Long requestUserId) {
        AssignmentAttachment attachment = assignmentAttachmentRepository.findByIdAndIsActiveTrue(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found: " + attachmentId));

        if (!attachment.getUploadedBy().equals(requestUserId)) {
            throw new RuntimeException("You are not authorized to delete this attachment");
        }

        localFileStorageService.deleteFile(attachment.getStoragePath());
        attachment.setIsActive(false);
        assignmentAttachmentRepository.save(attachment);
    }

    private AttachmentResponse toResponse(AssignmentAttachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .assignmentId(attachment.getAssignmentId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .uploadedBy(attachment.getUploadedBy())
                .uploadedAt(attachment.getUploadedAt())
                .downloadUrl("/assignments/attachments/" + attachment.getId() + "/download")
                .build();
    }

    public record DownloadFilePayload(Resource resource, String fileName) {
    }
}
