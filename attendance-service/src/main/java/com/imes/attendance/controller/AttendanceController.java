package com.imes.attendance.controller;

import com.imes.common.dto.ApiResponse;
import com.imes.common.dto.request.CheckInRequest;
import com.imes.common.dto.request.CheckOutRequest;
import com.imes.common.dto.request.LeaveRequest;
import com.imes.common.dto.response.AttendanceResponse;
import com.imes.common.dto.response.AttendanceStatisticsResponse;
import com.imes.common.dto.response.AttendanceMonthlySummaryResponse;
import com.imes.common.dto.response.AttendanceTrendsResponse;
import com.imes.common.dto.response.AttendanceAnomalyResponse;
import com.imes.common.dto.response.PunctualityDistributionResponse;
import com.imes.common.dto.response.TopPerformerResponse;
import com.imes.attendance.service.AttendanceReportService;
import com.imes.core.service.AttendanceAnomalyDetectionService;
import com.imes.core.service.AttendanceAnalyticsService;
import com.imes.core.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/attendances")
@CrossOrigin(origins = "*")
public class AttendanceController {
    
    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);
    private final AttendanceService attendanceService;
    private final AttendanceAnalyticsService attendanceAnalyticsService;
    private final AttendanceAnomalyDetectionService anomalyDetectionService;
    private final AttendanceReportService attendanceReportService;

    public AttendanceController(AttendanceService attendanceService,
                                AttendanceAnalyticsService attendanceAnalyticsService,
                                AttendanceAnomalyDetectionService anomalyDetectionService,
                                AttendanceReportService attendanceReportService) {
        this.attendanceService = attendanceService;
        this.attendanceAnalyticsService = attendanceAnalyticsService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.attendanceReportService = attendanceReportService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getAllAttendances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "INTERN", "MENTOR", "HR", "ADMIN");
        logger.info("[ATTENDANCE-SERVICE] Get all attendances - page: {}, size: {}", page, size);
        Page<AttendanceResponse> attendances = attendanceService.getAll(page, size);
        return ResponseEntity.ok(ApiResponse.success(attendances));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "INTERN", "MENTOR", "HR", "ADMIN");
        logger.info("[ATTENDANCE-SERVICE] Get attendance by id: {}", id);
        AttendanceResponse attendance = attendanceService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @GetMapping("/intern/{internId}")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getAttendancesByInternId(
            @PathVariable Long internId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "INTERN", "MENTOR", "HR", "ADMIN");
        logger.info("[ATTENDANCE-SERVICE] Get attendances for intern: {}", internId);
        Page<AttendanceResponse> attendances = attendanceService.getByInternProfile(internId, page, size);
        return ResponseEntity.ok(ApiResponse.success(attendances));
    }

    @GetMapping("/intern/{internId}/statistics")
    public ResponseEntity<ApiResponse<AttendanceStatisticsResponse>> getAttendanceStatistics(
            @PathVariable Long internId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "INTERN", "MENTOR", "HR", "ADMIN");
        logger.info("[ATTENDANCE-SERVICE] Get attendance statistics for intern: {}, from {} to {}", 
                    internId, startDate, endDate);
        AttendanceStatisticsResponse statistics = attendanceService.getStatistics(internId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/intern/{internId}/monthly-report")
    public ResponseEntity<ApiResponse<AttendanceStatisticsResponse>> getMonthlyReport(
            @PathVariable Long internId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "INTERN", "MENTOR", "HR", "ADMIN");
        logger.info("[ATTENDANCE-SERVICE] Get monthly report for intern: {}, year: {}, month: {}", 
                    internId, year, month);
        AttendanceStatisticsResponse report = attendanceService.getMonthlyReport(internId, year, month);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(
            @RequestBody CheckInRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireRole(userRole, "INTERN");
        logger.info("[ATTENDANCE-SERVICE] Check-in request for intern: {}, date: {}", 
                    request.internProfileId(), request.date());
        AttendanceResponse attendance = attendanceService.checkIn(request);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(
            @RequestBody CheckOutRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireRole(userRole, "INTERN");
        logger.info("[ATTENDANCE-SERVICE] Check-out request for attendance: {}", request.attendanceId());
        AttendanceResponse attendance = attendanceService.checkOut(request);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @PostMapping("/leave")
    public ResponseEntity<ApiResponse<AttendanceResponse>> requestLeave(
            @RequestBody LeaveRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireRole(userRole, "INTERN");
        logger.info("[ATTENDANCE-SERVICE] Leave request for intern: {}, date: {}", 
                    request.internProfileId(), request.leaveDate());
        AttendanceResponse attendance = attendanceService.requestLeave(request);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @PutMapping("/{id}/approve-leave")
    public ResponseEntity<ApiResponse<AttendanceResponse>> approveLeave(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        logger.info("[ATTENDANCE-SERVICE] Approve leave for attendance: {}, by: {}", id, approverId);
        AttendanceResponse attendance = attendanceService.approveLeave(id, approverId);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @PostMapping("/mark-absent")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAbsent(
            @RequestParam Long internId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        logger.info("[ATTENDANCE-SERVICE] Mark absent for intern: {}, date: {}", internId, date);
        AttendanceResponse attendance = attendanceService.markAbsent(internId, date);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Attendance Service is running"));
    }

    @GetMapping("/analytics/monthly-summary")
    public ResponseEntity<ApiResponse<AttendanceMonthlySummaryResponse>> getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        AttendanceMonthlySummaryResponse response = attendanceAnalyticsService.getMonthlySummary(year, month);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics/trends")
    public ResponseEntity<ApiResponse<AttendanceTrendsResponse>> getTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") String granularity,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        AttendanceTrendsResponse response = attendanceAnalyticsService.getTrends(startDate, endDate, granularity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics/top-performers")
    public ResponseEntity<ApiResponse<List<TopPerformerResponse>>> getTopPerformers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "5") int limit,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        List<TopPerformerResponse> response = attendanceAnalyticsService.getTopPerformers(startDate, endDate, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics/punctuality-distribution")
    public ResponseEntity<ApiResponse<PunctualityDistributionResponse>> getPunctualityDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        PunctualityDistributionResponse response = attendanceAnalyticsService.getPunctualityDistribution(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/reports/monthly")
    public ResponseEntity<byte[]> downloadMonthlyReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "PDF") String format,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        AttendanceReportService.ReportFile report = attendanceReportService.generateMonthlyReport(year, month, format);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.fileName() + "\"")
            .contentType(MediaType.parseMediaType(Objects.requireNonNull(report.contentType())))
                .contentLength(report.content().length)
                .body(report.content());
    }

    @PostMapping("/anomalies/detect")
    public ResponseEntity<ApiResponse<List<AttendanceAnomalyResponse>>> detectAnomalies(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        List<AttendanceAnomalyResponse> response = anomalyDetectionService.detectInRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/anomalies")
    public ResponseEntity<ApiResponse<List<AttendanceAnomalyResponse>>> getAnomalies(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        requireAnyRole(userRole, "MENTOR", "HR", "ADMIN");
        List<AttendanceAnomalyResponse> response = anomalyDetectionService.getAnomalies(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
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
