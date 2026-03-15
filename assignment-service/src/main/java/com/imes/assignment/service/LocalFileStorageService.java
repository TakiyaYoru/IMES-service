package com.imes.assignment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class LocalFileStorageService {

    private static final DateTimeFormatter NAME_TS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Value("${imes.file-upload.base-dir:uploads}")
    private String baseDir;

    @Value("${imes.file-upload.max-size-bytes:10485760}")
    private long maxSizeBytes;

    @Value("${imes.file-upload.allowed-types:pdf,doc,docx,zip,jpg,jpeg,png}")
    private List<String> allowedTypes;

    public StoredFileInfo uploadFile(MultipartFile file, String category) {
        validateFile(file);

        String safeCategory = sanitizePathSegment(category == null ? "general" : category);
        String extension = getExtension(file.getOriginalFilename());
        String safeOriginalName = sanitizeFileName(file.getOriginalFilename());
        String uniqueName = LocalDateTime.now().format(NAME_TS_FORMAT) + "-" + UUID.randomUUID() + "-" + safeOriginalName;

        Path categoryDir = Paths.get(baseDir, safeCategory, LocalDate.now().toString()).normalize().toAbsolutePath();
        ensureSafePath(categoryDir);

        try {
            Files.createDirectories(categoryDir);
            Path destination = categoryDir.resolve(uniqueName).normalize();
            ensureSafePath(destination);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            return new StoredFileInfo(
                    safeOriginalName,
                    destination.toString(),
                    extension,
                    file.getSize()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public Path resolveDownloadPath(String storedPath) {
        Path path = Paths.get(storedPath).normalize().toAbsolutePath();
        ensureSafePath(path);

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new RuntimeException("File not found");
        }

        return path;
    }

    public void deleteFile(String storedPath) {
        Path path = Paths.get(storedPath).normalize().toAbsolutePath();
        ensureSafePath(path);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }

        if (file.getSize() > maxSizeBytes) {
            throw new RuntimeException("File exceeds max size of 10MB");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (extension.isBlank() || allowedTypes.stream().noneMatch(ext -> ext.equalsIgnoreCase(extension))) {
            throw new RuntimeException("File type is not allowed");
        }
    }

    private String getExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private String sanitizeFileName(String fileName) {
        String normalized = StringUtils.hasText(fileName) ? fileName : "file";
        String cleaned = Paths.get(normalized).getFileName().toString();
        return cleaned.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String sanitizePathSegment(String segment) {
        return segment.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private void ensureSafePath(Path path) {
        Path root = Paths.get(baseDir).normalize().toAbsolutePath();
        if (!path.startsWith(root)) {
            throw new RuntimeException("Invalid file path");
        }
    }

    public record StoredFileInfo(
            String originalFileName,
            String storedPath,
            String fileType,
            Long fileSize
    ) {
    }
}
