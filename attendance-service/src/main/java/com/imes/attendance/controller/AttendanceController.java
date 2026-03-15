package com.imes.attendance.controller;

import com.imes.common.dto.ApiResponse;
import com.imes.common.dto.request.CheckInRequest;
import com.imes.common.dto.request.CheckOutRequest;
import com.imes.common.dto.request.LeaveRequest;
import com.imes.common.dto.response.AttendanceResponse;
import com.imes.common.dto.response.AttendanceStatisticsResponse;
import com.imes.core.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/attendances")
@CrossOrigin(origins = "*")
public class AttendanceController {
    
    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getAllAttendances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("[ATTENDANCE-SERVICE] Get all attendances - page: {}, size: {}", page, size);
        Page<AttendanceResponse> attendances = attendanceService.getAll(page, size);
        return ResponseEntity.ok(ApiResponse.success(attendances));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceById(@PathVariable Long id) {
        logger.info("[ATTENDANCE-SERVICE] Get attendance by id: {}", id);
        AttendanceResponse attendance = attendanceService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @GetMapping("/intern/{internId}")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getAttendancesByInternId(
            @PathVariable Long internId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("[ATTENDANCE-SERVICE] Get attendances for intern: {}", internId);
        Page<AttendanceResponse> attendances = attendanceService.getByInternProfile(internId, page, size);
        return ResponseEntity.ok(ApiResponse.success(attendances));
    }

    @GetMapping("/intern/{internId}/statistics")
    public ResponseEntity<ApiResponse<AttendanceStatisticsResponse>> getAttendanceStatistics(
            @PathVariable Long internId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.info("[ATTENDANCE-SERVICE] Get attendance statistics for intern: {}, from {} to {}", 
                    internId, startDate, endDate);
        AttendanceStatisticsResponse statistics = attendanceService.getStatistics(internId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/intern/{internId}/monthly-report")
    public ResponseEntity<ApiResponse<AttendanceStatisticsResponse>> getMonthlyReport(
            @PathVariable Long internId,
            @RequestParam int year,
            @RequestParam int month) {
        logger.info("[ATTENDANCE-SERVICE] Get monthly report for intern: {}, year: {}, month: {}", 
                    internId, year, month);
        AttendanceStatisticsResponse report = attendanceService.getMonthlyReport(internId, year, month);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(@RequestBody CheckInRequest request) {
        logger.info("[ATTENDANCE-SERVICE] Check-in request for intern: {}, date: {}", 
                    request.internProfileId(), request.date());
        AttendanceResponse attendance = attendanceService.checkIn(request);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(@RequestBody CheckOutRequest request) {
        logger.info("[ATTENDANCE-SERVICE] Check-out request for attendance: {}", request.attendanceId());
        AttendanceResponse attendance = attendanceService.checkOut(request);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @PostMapping("/leave")
    public ResponseEntity<ApiResponse<AttendanceResponse>> requestLeave(@RequestBody LeaveRequest request) {
        logger.info("[ATTENDANCE-SERVICE] Leave request for intern: {}, date: {}", 
                    request.internProfileId(), request.leaveDate());
        AttendanceResponse attendance = attendanceService.requestLeave(request);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @PutMapping("/{id}/approve-leave")
    public ResponseEntity<ApiResponse<AttendanceResponse>> approveLeave(
            @PathVariable Long id,
            @RequestParam Long approverId) {
        logger.info("[ATTENDANCE-SERVICE] Approve leave for attendance: {}, by: {}", id, approverId);
        AttendanceResponse attendance = attendanceService.approveLeave(id, approverId);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @PostMapping("/mark-absent")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAbsent(
            @RequestParam Long internId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        logger.info("[ATTENDANCE-SERVICE] Mark absent for intern: {}, date: {}", internId, date);
        AttendanceResponse attendance = attendanceService.markAbsent(internId, date);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Attendance Service is running"));
    }
}
