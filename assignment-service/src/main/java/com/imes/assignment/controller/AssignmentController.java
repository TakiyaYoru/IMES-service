package com.imes.assignment.controller;

import com.imes.assignment.dto.*;
import com.imes.assignment.service.AssignmentAnalyticsService;
import com.imes.assignment.service.AssignmentService;
import com.imes.common.dto.ResponseApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
@Slf4j
public class AssignmentController {
    
    private final AssignmentService assignmentService;
    private final AssignmentAnalyticsService assignmentAnalyticsService;
    
    @GetMapping("/health")
    public ResponseApi<String> health() {
        return ResponseApi.success("Assignment Service is running");
    }
    
    @PostMapping
    public ResponseApi<AssignmentResponse> createAssignment(
            @Valid @RequestBody CreateAssignmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long mentorId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        mentorId = requireUserId(mentorId);
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        log.info("Creating assignment for mentor: {}", mentorId);
        AssignmentResponse response = assignmentService.createAssignment(request, mentorId);
        return ResponseApi.success(response);
    }
    
    @GetMapping("/intern/{internId}")
    public ResponseApi<Page<AssignmentResponse>> getAssignmentsByIntern(
            @PathVariable Long internId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        log.info("Fetching assignments for intern: {}", internId);
        Page<AssignmentResponse> assignments = assignmentService.getAssignmentsByIntern(internId, page, size);
        return ResponseApi.success(assignments);
    }

    @GetMapping("/my-assignments")
    public ResponseApi<Page<AssignmentResponse>> getMyAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Id", required = false) Long mentorId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        mentorId = requireUserId(mentorId);
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        Page<AssignmentResponse> assignments = assignmentService.getAssignmentsByMentor(mentorId, page, size);
        return ResponseApi.success(assignments);
    }
    
    @GetMapping("/{id}")
    public ResponseApi<AssignmentResponse> getAssignment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "INTERN", "HR", "ADMIN");
        AssignmentResponse response = assignmentService.getAssignmentById(id);
        return ResponseApi.success(response);
    }

    @PutMapping("/{id}")
    public ResponseApi<AssignmentResponse> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long mentorId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        mentorId = requireUserId(mentorId);
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        AssignmentResponse response = assignmentService.updateAssignment(id, request, mentorId);
        return ResponseApi.success(response);
    }

    @DeleteMapping("/{id}")
    public ResponseApi<String> deleteAssignment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long mentorId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        mentorId = requireUserId(mentorId);
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        assignmentService.deleteAssignment(id, mentorId);
        return ResponseApi.success("Assignment deleted successfully");
    }
    
    @PostMapping("/{id}/submit")
    public ResponseApi<SubmissionResponse> submitAssignment(
            @PathVariable Long id,
            @Valid @RequestBody SubmitAssignmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long internId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        internId = requireUserId(internId);
        requireRole(userRole, "INTERN");
        log.info("Intern {} submitting assignment {}", internId, id);
        SubmissionResponse response = assignmentService.submitAssignment(id, request, internId);
        return ResponseApi.success(response);
    }
    
    @GetMapping("/{id}/submissions")
    public ResponseApi<List<SubmissionResponse>> getSubmissions(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        List<SubmissionResponse> submissions = assignmentService.getSubmissionsByAssignment(id);
        return ResponseApi.success(submissions);
    }
    
    @PutMapping("/{id}/complete")
    public ResponseApi<AssignmentResponse> markAsCompleted(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        AssignmentResponse response = assignmentService.markAsCompleted(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/publish")
    public ResponseApi<AssignmentResponse> publishAssignment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        AssignmentResponse response = assignmentService.publishAssignment(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/accept")
    public ResponseApi<AssignmentResponse> acceptAssignment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        AssignmentResponse response = assignmentService.acceptAssignment(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/reject")
    public ResponseApi<AssignmentResponse> rejectAssignment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        AssignmentResponse response = assignmentService.rejectAssignment(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/start")
    public ResponseApi<AssignmentResponse> startAssignment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        AssignmentResponse response = assignmentService.startAssignment(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/submit-work")
    public ResponseApi<AssignmentResponse> submitWork(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "INTERN", "HR", "ADMIN");
        AssignmentResponse response = assignmentService.submitWorkAssignment(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/request-revision")
    public ResponseApi<AssignmentResponse> requestRevision(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        AssignmentResponse response = assignmentService.requestRevision(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/approve")
    public ResponseApi<AssignmentResponse> approveAssignment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        AssignmentResponse response = assignmentService.approveAssignment(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseApi<AssignmentResponse> cancelAssignment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        AssignmentResponse response = assignmentService.cancelAssignment(id);
        return ResponseApi.success(response);
    }

    @PutMapping("/bulk-status")
    public ResponseApi<List<AssignmentResponse>> bulkUpdateStatus(
            @Valid @RequestBody BulkUpdateAssignmentStatusRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        List<AssignmentResponse> response = assignmentService.bulkUpdateStatus(request);
        return ResponseApi.success(response);
    }

    @GetMapping("/analytics/completion-rate")
    public ResponseApi<CompletionRateResponse> getCompletionRate(
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        CompletionRateResponse response = assignmentAnalyticsService.getCompletionRate();
        return ResponseApi.success(response);
    }

    @GetMapping("/analytics/time-estimation-accuracy")
    public ResponseApi<TimeEstimationAccuracyResponse> getTimeEstimationAccuracy(
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        TimeEstimationAccuracyResponse response = assignmentAnalyticsService.getTimeEstimationAccuracy();
        return ResponseApi.success(response);
    }

    @GetMapping("/analytics/overdue-summary")
    public ResponseApi<OverdueSummaryResponse> getOverdueSummary(
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        OverdueSummaryResponse response = assignmentAnalyticsService.getOverdueSummary();
        return ResponseApi.success(response);
    }

    @GetMapping("/analytics/workload-distribution")
    public ResponseApi<WorkloadDistributionResponse> getWorkloadDistribution(
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        WorkloadDistributionResponse response = assignmentAnalyticsService.getWorkloadDistribution();
        return ResponseApi.success(response);
    }
    
    @PutMapping("/submissions/{submissionId}/review")
    public ResponseApi<SubmissionResponse> reviewSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody ReviewSubmissionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long mentorId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        mentorId = requireUserId(mentorId);
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        log.info("Mentor {} reviewing submission {}", mentorId, submissionId);
        SubmissionResponse response = assignmentService.reviewSubmission(submissionId, request, mentorId);
        return ResponseApi.success(response);
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Missing required header: X-User-Id");
        }
        return userId;
    }

    private void requireRole(String role, String expectedRole) {
        if (role == null || !expectedRole.equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Forbidden: required role " + expectedRole);
        }
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
