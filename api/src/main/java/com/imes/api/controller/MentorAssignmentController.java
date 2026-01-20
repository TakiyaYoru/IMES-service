package com.imes.api.controller;

import com.imes.common.dto.ResponseApi;
import com.imes.common.dto.request.CreateMentorAssignmentRequest;
import com.imes.common.dto.request.UpdateMentorAssignmentRequest;
import com.imes.common.dto.response.MentorAssignmentResponse;
import com.imes.core.service.MentorAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/mentor-assignments")
@RequiredArgsConstructor
@Slf4j
public class MentorAssignmentController {

    private final MentorAssignmentService mentorAssignmentService;

    /**
     * Create a new mentor assignment
     * POST /mentor-assignments
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ResponseApi<MentorAssignmentResponse>> createAssignment(
            @Valid @RequestBody CreateMentorAssignmentRequest request) {
        log.info("Create mentor assignment request");
        MentorAssignmentResponse response = mentorAssignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(response));
    }

    /**
     * Get assignment by ID
     * GET /mentor-assignments/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR')")
    public ResponseEntity<ResponseApi<MentorAssignmentResponse>> getAssignment(@PathVariable Long id) {
        log.info("Get mentor assignment: {}", id);
        MentorAssignmentResponse response = mentorAssignmentService.getById(id);
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    /**
     * Get all assignments
     * GET /mentor-assignments?page=1&size=10
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR')")
    public ResponseEntity<ResponseApi<Page<MentorAssignmentResponse>>> getAllAssignments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get all mentor assignments: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<MentorAssignmentResponse> response = mentorAssignmentService.getAll(pageable);
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    /**
     * Get assignments by mentor ID
     * GET /mentor-assignments/mentor/{mentorId}?page=1&size=10
     */
    @GetMapping("/mentor/{mentorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR')")
    public ResponseEntity<ResponseApi<Page<MentorAssignmentResponse>>> getByMentor(
            @PathVariable Long mentorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get assignments by mentor: {}", mentorId);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<MentorAssignmentResponse> response = mentorAssignmentService.getByMentorId(mentorId, pageable);
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    /**
     * Get assignments by intern profile ID
     * GET /mentor-assignments/intern/{internProfileId}?page=1&size=10
     */
    @GetMapping("/intern/{internProfileId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR')")
    public ResponseEntity<ResponseApi<Page<MentorAssignmentResponse>>> getByIntern(
            @PathVariable Long internProfileId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get assignments by intern: {}", internProfileId);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<MentorAssignmentResponse> response = mentorAssignmentService.getByInternProfileId(internProfileId, pageable);
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    /**
     * Search assignments by keyword
     * GET /mentor-assignments/search?keyword=...&page=1&size=10
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR')")
    public ResponseEntity<ResponseApi<Page<MentorAssignmentResponse>>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Search mentor assignments: keyword={}", keyword);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<MentorAssignmentResponse> response = mentorAssignmentService.search(keyword, pageable);
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    /**
     * Get assignments by status
     * GET /mentor-assignments/status/{status}?page=1&size=10
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR')")
    public ResponseEntity<ResponseApi<Page<MentorAssignmentResponse>>> getByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get assignments by status: {}", status);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<MentorAssignmentResponse> response = mentorAssignmentService.getByStatus(status, pageable);
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    /**
     * Update assignment
     * PUT /mentor-assignments/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ResponseApi<MentorAssignmentResponse>> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMentorAssignmentRequest request) {
        log.info("Update mentor assignment: {}", id);
        MentorAssignmentResponse response = mentorAssignmentService.update(id, request);
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    /**
     * Update assignment status
     * PATCH /mentor-assignments/{id}/status?status=ACTIVE
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ResponseApi<MentorAssignmentResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("Update assignment status: id={}, status={}", id, status);
        MentorAssignmentResponse response = mentorAssignmentService.updateStatus(id, status);
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    /**
     * Update start date
     * PATCH /mentor-assignments/{id}/start-date?startDate=2026-01-20
     */
    @PatchMapping("/{id}/start-date")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ResponseApi<MentorAssignmentResponse>> updateStartDate(
            @PathVariable Long id,
            @RequestParam LocalDate startDate) {
        log.info("Update start date: id={}, startDate={}", id, startDate);
        MentorAssignmentResponse response = mentorAssignmentService.updateStartDate(id, startDate);
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    /**
     * Update end date
     * PATCH /mentor-assignments/{id}/end-date?endDate=2026-06-20
     */
    @PatchMapping("/{id}/end-date")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ResponseApi<MentorAssignmentResponse>> updateEndDate(
            @PathVariable Long id,
            @RequestParam LocalDate endDate) {
        log.info("Update end date: id={}, endDate={}", id, endDate);
        MentorAssignmentResponse response = mentorAssignmentService.updateEndDate(id, endDate);
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    /**
     * Delete (soft delete) assignment
     * DELETE /mentor-assignments/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ResponseApi<Void>> deleteAssignment(@PathVariable Long id) {
        log.info("Delete mentor assignment: {}", id);
        mentorAssignmentService.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null));
    }

    /**
     * Get total count
     * GET /mentor-assignments/statistics/total
     */
    @GetMapping("/statistics/total")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR')")
    public ResponseEntity<ResponseApi<Long>> getTotalCount() {
        log.info("Get total mentor assignments count");
        long count = mentorAssignmentService.getTotalCount();
        return ResponseEntity.ok(ResponseApi.success(count));
    }
}
