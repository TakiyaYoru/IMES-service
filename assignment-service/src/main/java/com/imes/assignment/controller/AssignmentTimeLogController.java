package com.imes.assignment.controller;

import com.imes.assignment.dto.CreateTimeLogRequest;
import com.imes.assignment.dto.TimeLogListResponse;
import com.imes.assignment.dto.TimeLogResponse;
import com.imes.assignment.service.AssignmentTimeLogService;
import com.imes.common.dto.ResponseApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AssignmentTimeLogController {

    private final AssignmentTimeLogService assignmentTimeLogService;

    @PostMapping("/assignments/{id}/time-logs")
    public ResponseApi<TimeLogResponse> addTimeLog(
            @PathVariable("id") Long assignmentId,
            @Valid @RequestBody CreateTimeLogRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long internId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        internId = requireUserId(internId);
        requireRole(userRole, "INTERN");
        TimeLogResponse response = assignmentTimeLogService.addTimeLog(assignmentId, request, internId);
        return ResponseApi.success(response);
    }

    @GetMapping("/assignments/{id}/time-logs")
    public ResponseApi<TimeLogListResponse> getAssignmentTimeLogs(
            @PathVariable("id") Long assignmentId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "INTERN", "MENTOR", "HR", "ADMIN");
        TimeLogListResponse response = assignmentTimeLogService.getByAssignment(assignmentId);
        return ResponseApi.success(response);
    }

    @GetMapping("/interns/{internId}/time-logs")
    public ResponseApi<TimeLogListResponse> getInternTimeLogs(
            @PathVariable Long internId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireAnyRole(userRole, "INTERN", "MENTOR", "HR", "ADMIN");
        TimeLogListResponse response = assignmentTimeLogService.getByIntern(internId);
        return ResponseApi.success(response);
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Missing required header: X-User-Id");
        }
        return userId;
    }

    private void requireRole(String actualRole, String expectedRole) {
        if (actualRole == null || !expectedRole.equalsIgnoreCase(actualRole)) {
            throw new IllegalArgumentException("Forbidden: role " + actualRole + " is not allowed");
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
