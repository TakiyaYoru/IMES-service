package com.imes.assignment.controller;

import com.imes.assignment.dto.*;
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
    
    @GetMapping("/health")
    public ResponseApi<String> health() {
        return ResponseApi.success("Assignment Service is running");
    }
    
    @PostMapping
    public ResponseApi<AssignmentResponse> createAssignment(
            @Valid @RequestBody CreateAssignmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "2") Long mentorId
    ) {
        log.info("Creating assignment for mentor: {}", mentorId);
        AssignmentResponse response = assignmentService.createAssignment(request, mentorId);
        return ResponseApi.success(response);
    }
    
    @GetMapping("/my-assignments")
    public ResponseApi<Page<AssignmentResponse>> getMyAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "2") Long mentorId
    ) {
        Page<AssignmentResponse> assignments = assignmentService.getAssignmentsByMentor(mentorId, page, size);
        return ResponseApi.success(assignments);
    }
    
    @GetMapping("/{id}")
    public ResponseApi<AssignmentResponse> getAssignment(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.getAssignmentById(id);
        return ResponseApi.success(response);
    }
    
    @PostMapping("/{id}/submit")
    public ResponseApi<SubmissionResponse> submitAssignment(
            @PathVariable Long id,
            @Valid @RequestBody SubmitAssignmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "4") Long internId
    ) {
        log.info("Intern {} submitting assignment {}", internId, id);
        SubmissionResponse response = assignmentService.submitAssignment(id, request, internId);
        return ResponseApi.success(response);
    }
    
    @GetMapping("/{id}/submissions")
    public ResponseApi<List<SubmissionResponse>> getSubmissions(@PathVariable Long id) {
        List<SubmissionResponse> submissions = assignmentService.getSubmissionsByAssignment(id);
        return ResponseApi.success(submissions);
    }
    
    @PutMapping("/{id}/complete")
    public ResponseApi<AssignmentResponse> markAsCompleted(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.markAsCompleted(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/publish")
    public ResponseApi<AssignmentResponse> publishAssignment(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.publishAssignment(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/accept")
    public ResponseApi<AssignmentResponse> acceptAssignment(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.acceptAssignment(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/reject")
    public ResponseApi<AssignmentResponse> rejectAssignment(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.rejectAssignment(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/start")
    public ResponseApi<AssignmentResponse> startAssignment(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.startAssignment(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/submit-work")
    public ResponseApi<AssignmentResponse> submitWork(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.submitWorkAssignment(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/request-revision")
    public ResponseApi<AssignmentResponse> requestRevision(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.requestRevision(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/approve")
    public ResponseApi<AssignmentResponse> approveAssignment(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.approveAssignment(id);
        return ResponseApi.success(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseApi<AssignmentResponse> cancelAssignment(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.cancelAssignment(id);
        return ResponseApi.success(response);
    }

    @PutMapping("/bulk-status")
    public ResponseApi<List<AssignmentResponse>> bulkUpdateStatus(
            @Valid @RequestBody BulkUpdateAssignmentStatusRequest request
    ) {
        List<AssignmentResponse> response = assignmentService.bulkUpdateStatus(request);
        return ResponseApi.success(response);
    }
    
    @PutMapping("/submissions/{submissionId}/review")
    public ResponseApi<SubmissionResponse> reviewSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody ReviewSubmissionRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "3") Long mentorId
    ) {
        log.info("Mentor {} reviewing submission {}", mentorId, submissionId);
        SubmissionResponse response = assignmentService.reviewSubmission(submissionId, request, mentorId);
        return ResponseApi.success(response);
    }
}
