package com.imes.intern.controller;

import com.imes.common.dto.ApiResponse;
import com.imes.common.dto.request.CreateMentorAssignmentRequest;
import com.imes.common.dto.request.UpdateMentorAssignmentRequest;
import com.imes.common.dto.response.MentorAssignmentResponse;
import com.imes.core.service.MentorAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mentor-assignments")
@CrossOrigin(origins = "*")
public class MentorAssignmentController {
    
    private static final Logger logger = LoggerFactory.getLogger(MentorAssignmentController.class);
    private final MentorAssignmentService mentorAssignmentService;

    public MentorAssignmentController(MentorAssignmentService mentorAssignmentService) {
        this.mentorAssignmentService = mentorAssignmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MentorAssignmentResponse>>> getAllAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("[INTERN-SERVICE] Get all mentor assignments - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<MentorAssignmentResponse> assignments = mentorAssignmentService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MentorAssignmentResponse>> getAssignmentById(@PathVariable Long id) {
        logger.info("[INTERN-SERVICE] Get mentor assignment by id: {}", id);
        MentorAssignmentResponse assignment = mentorAssignmentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(assignment));
    }

    @GetMapping("/intern/{internId}")
    public ResponseEntity<ApiResponse<Page<MentorAssignmentResponse>>> getAssignmentsByInternId(
            @PathVariable Long internId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("[INTERN-SERVICE] Get mentor assignments for intern: {}", internId);
        Pageable pageable = PageRequest.of(page, size);
        Page<MentorAssignmentResponse> assignments = mentorAssignmentService.getByInternProfileId(internId, pageable);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<ApiResponse<Page<MentorAssignmentResponse>>> getAssignmentsByMentorId(
            @PathVariable Long mentorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("[INTERN-SERVICE] Get mentor assignments for mentor: {}", mentorId);
        Pageable pageable = PageRequest.of(page, size);
        Page<MentorAssignmentResponse> assignments = mentorAssignmentService.getByMentorId(mentorId, pageable);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MentorAssignmentResponse>> createAssignment(@RequestBody CreateMentorAssignmentRequest request) {
        logger.info("[INTERN-SERVICE] Create mentor assignment: intern={}, mentor={}", 
                    request.internProfileId(), request.mentorId());
        MentorAssignmentResponse assignment = mentorAssignmentService.createAssignment(request);
        return ResponseEntity.ok(ApiResponse.success(assignment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MentorAssignmentResponse>> updateAssignment(
            @PathVariable Long id,
            @RequestBody UpdateMentorAssignmentRequest request) {
        logger.info("[INTERN-SERVICE] Update mentor assignment: id={}", id);
        MentorAssignmentResponse assignment = mentorAssignmentService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(assignment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long id) {
        logger.info("[INTERN-SERVICE] Delete mentor assignment: id={}", id);
        mentorAssignmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
