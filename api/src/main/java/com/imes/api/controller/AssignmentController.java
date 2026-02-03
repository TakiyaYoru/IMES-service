package com.imes.api.controller;

import com.imes.common.dto.request.CreateAssignmentRequest;
import com.imes.common.dto.request.UpdateAssignmentRequest;
import com.imes.common.dto.response.AssignmentResponse;
import com.imes.common.dto.ResponseApi;
import com.imes.core.service.AssignmentService;
import com.imes.core.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Assignment Management
 * Features: Create, Read, Update, Delete assignments
 * Only mentors can create/update/delete their own assignments
 */
@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
@Slf4j
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final UserService userService;

    /**
     * Create a new assignment (MENTOR only)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseApi<AssignmentResponse> createAssignment(
            @Valid @RequestBody CreateAssignmentRequest request,
            Authentication auth) {
        log.info("POST /assignments - Create assignment: {}", request.title());
        
        // Get mentor ID from authentication
        String email = auth.getName();
        Long mentorId = userService.getUserByEmail(email).getId();
        
        AssignmentResponse response = assignmentService.createAssignment(request, mentorId);
        return ResponseApi.success(response);
    }

    /**
     * Get assignment by ID (authenticated users)
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseApi<AssignmentResponse> getAssignmentById(@PathVariable Long id) {
        log.info("GET /assignments/{} - Get assignment by ID", id);
        
        AssignmentResponse response = assignmentService.getAssignmentById(id);
        return ResponseApi.success(response);
    }

    /**
     * Update assignment (MENTOR only - owner only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseApi<AssignmentResponse> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentRequest request,
            Authentication auth) {
        log.info("PUT /assignments/{} - Update assignment", id);
        
        // Get mentor ID from authentication
        String email = auth.getName();
        Long mentorId = userService.getUserByEmail(email).getId();
        
        AssignmentResponse response = assignmentService.updateAssignment(id, request, mentorId);
        return ResponseApi.success(response);
    }

    /**
     * Delete assignment (MENTOR only - owner only)
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseApi<Void> deleteAssignment(
            @PathVariable Long id,
            Authentication auth) {
        log.info("DELETE /assignments/{} - Delete assignment", id);
        
        // Get mentor ID from authentication
        String email = auth.getName();
        Long mentorId = userService.getUserByEmail(email).getId();
        
        assignmentService.deleteAssignment(id, mentorId);
        return ResponseApi.success(null);
    }

    /**
     * Get all assignments (paginated) - ADMIN/HR only
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseApi<Page<AssignmentResponse>> getAllAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /assignments - Get all assignments (page: {}, size: {})", page, size);
        
        Page<AssignmentResponse> response = assignmentService.getAllAssignments(page, size);
        return ResponseApi.success(response);
    }

    /**
     * Get assignments by mentor
     */
    @GetMapping("/mentor/{mentorId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN', 'HR')")
    public ResponseApi<List<AssignmentResponse>> getAssignmentsByMentor(@PathVariable Long mentorId) {
        log.info("GET /assignments/mentor/{} - Get assignments by mentor", mentorId);
        
        List<AssignmentResponse> response = assignmentService.getAssignmentsByMentor(mentorId);
        return ResponseApi.success(response);
    }
}
