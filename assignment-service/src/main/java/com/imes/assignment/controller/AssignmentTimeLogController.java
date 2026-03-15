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
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "4") Long internId
    ) {
        TimeLogResponse response = assignmentTimeLogService.addTimeLog(assignmentId, request, internId);
        return ResponseApi.success(response);
    }

    @GetMapping("/assignments/{id}/time-logs")
    public ResponseApi<TimeLogListResponse> getAssignmentTimeLogs(@PathVariable("id") Long assignmentId) {
        TimeLogListResponse response = assignmentTimeLogService.getByAssignment(assignmentId);
        return ResponseApi.success(response);
    }

    @GetMapping("/interns/{internId}/time-logs")
    public ResponseApi<TimeLogListResponse> getInternTimeLogs(@PathVariable Long internId) {
        TimeLogListResponse response = assignmentTimeLogService.getByIntern(internId);
        return ResponseApi.success(response);
    }
}
