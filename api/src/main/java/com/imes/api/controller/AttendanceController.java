package com.imes.api.controller;

import com.imes.common.dto.request.CheckInRequest;
import com.imes.common.dto.request.CheckOutRequest;
import com.imes.common.dto.request.LeaveRequest;
import com.imes.common.dto.response.AttendanceResponse;
import com.imes.common.dto.ResponseApi;
import com.imes.core.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Attendance Management
 * 
 * Endpoints:
 * - POST /check-in: Intern checks in
 * - POST /check-out: Intern checks out
 * - GET /{id}: Get attendance by ID
 * - GET /intern/{internId}: List intern's attendance history
 */
@RestController
@RequestMapping("/attendances")
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Check-in endpoint
     * - Auto-detects LATE status if after 9 AM
     * - Prevents duplicate check-ins
     */
    @PostMapping("/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INTERN', 'ADMIN')")
    public ResponseApi<AttendanceResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        log.info("Check-in request from intern: {}", request.internProfileId());
        AttendanceResponse response = attendanceService.checkIn(request);
        return ResponseApi.success(response);
    }

    /**
     * Check-out endpoint
     * - Calculates total hours worked
     * - Updates status if less than half day
     */
    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('INTERN', 'ADMIN')")
    public ResponseApi<AttendanceResponse> checkOut(@Valid @RequestBody CheckOutRequest request) {
        log.info("Check-out request for attendance: {}", request.attendanceId());
        AttendanceResponse response = attendanceService.checkOut(request);
        return ResponseApi.success(response);
    }

    /**
     * Request leave in advance
     */
    @PostMapping("/leave-request")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INTERN', 'ADMIN')")
    public ResponseApi<AttendanceResponse> requestLeave(@Valid @RequestBody LeaveRequest request) {
        log.info("Leave request from intern: {} for date: {}", request.internProfileId(), request.leaveDate());
        AttendanceResponse response = attendanceService.requestLeave(request);
        return ResponseApi.success(response);
    }

    /**
     * Get attendance by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INTERN', 'MENTOR', 'HR', 'ADMIN')")
    public ResponseApi<AttendanceResponse> getById(@PathVariable Long id) {
        log.info("Get attendance by id: {}", id);
        AttendanceResponse response = attendanceService.getById(id);
        return ResponseApi.success(response);
    }

    /**
     * Get all attendance records for an intern (paginated)
     */
    @GetMapping("/intern/{internId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'HR', 'ADMIN')")
    public ResponseApi<Page<AttendanceResponse>> getByIntern(
            @PathVariable Long internId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get attendance for intern: {}, page: {}, size: {}", internId, page, size);
        Page<AttendanceResponse> responses = attendanceService.getByInternProfile(internId, page, size);
        return ResponseApi.success(responses);
    }
}
