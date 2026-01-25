package com.imes.core.service;

import com.imes.common.dto.request.CheckInRequest;
import com.imes.common.dto.request.CheckOutRequest;
import com.imes.common.dto.request.LeaveRequest;
import com.imes.common.dto.response.AttendanceResponse;
import com.imes.common.dto.response.AttendanceStatisticsResponse;
import com.imes.core.exception.ClientSideException;
import com.imes.core.exception.ErrorCode;
import com.imes.infra.entity.AttendanceEntity;
import com.imes.infra.entity.AttendanceStatus;
import com.imes.infra.entity.InternProfileEntity;
import com.imes.infra.repository.AttendanceRepository;
import com.imes.infra.repository.InternProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final InternProfileRepository internProfileRepository;
    
    private static final LocalTime STANDARD_CHECK_IN_TIME = LocalTime.of(9, 0);
    private static final LocalTime HALF_DAY_THRESHOLD = LocalTime.of(13, 0);
    private static final int STANDARD_WORK_HOURS = 8;

    /**
     * Check-in for intern
     * - Auto-detect LATE if check-in after 9:00 AM
     * - Prevent duplicate check-ins on same date
     */
    @Transactional
    public AttendanceResponse checkIn(CheckInRequest request) {
        log.info("Processing check-in for intern: {}, date: {}", request.internProfileId(), request.date());
        
        // Validate intern exists
        InternProfileEntity intern = internProfileRepository.findByIdAndIsActiveTrue(request.internProfileId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.INTERN_PROFILE_NOT_FOUND));
        
        // Prevent duplicate check-ins
        if (attendanceRepository.existsByInternAndDate(intern.getId(), request.date())) {
            throw new ClientSideException(ErrorCode.DUPLICATE_CHECK_IN);
        }
        
        // Determine check-in time
        LocalTime checkInTime = request.checkInTime() != null ? request.checkInTime() : LocalTime.now();
        
        // Auto-detect status based on check-in time
        AttendanceStatus status = checkInTime.isAfter(STANDARD_CHECK_IN_TIME) 
                ? AttendanceStatus.LATE 
                : AttendanceStatus.PRESENT;
        
        // Create attendance record
        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setInternProfileId(intern.getId());
        attendance.setDate(request.date());
        attendance.setCheckInTime(checkInTime);
        attendance.setStatus(status);
        attendance.setIsActive(true);
        
        AttendanceEntity saved = attendanceRepository.save(attendance);
        log.info("Check-in successful: attendanceId={}, status={}", saved.getId(), status);
        
        return mapToResponse(saved);
    }

    /**
     * Check-out for intern
     * - Calculate total hours worked
     * - Update status if less than half day
     */
    @Transactional
    public AttendanceResponse checkOut(CheckOutRequest request) {
        log.info("Processing check-out for attendance: {}", request.attendanceId());
        
        // Find attendance record
        AttendanceEntity attendance = attendanceRepository.findByIdAndIsActiveTrue(request.attendanceId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.ATTENDANCE_NOT_FOUND));
        
        // Validate already checked in
        if (attendance.getCheckInTime() == null) {
            throw new ClientSideException(ErrorCode.CHECK_IN_REQUIRED);
        }
        
        // Validate not already checked out
        if (attendance.getCheckOutTime() != null) {
            throw new ClientSideException(ErrorCode.ALREADY_CHECKED_OUT);
        }
        
        // Determine check-out time
        LocalTime checkOutTime = request.checkOutTime() != null ? request.checkOutTime() : LocalTime.now();
        
        // Validate check-out after check-in
        if (checkOutTime.isBefore(attendance.getCheckInTime())) {
            throw new ClientSideException(ErrorCode.INVALID_CHECK_OUT_TIME);
        }
        
        // Calculate total hours
        long minutes = ChronoUnit.MINUTES.between(attendance.getCheckInTime(), checkOutTime);
        BigDecimal totalHours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        
        // Update status if worked less than half day (4 hours)
        if (totalHours.compareTo(BigDecimal.valueOf(4)) < 0) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }
        
        attendance.setCheckOutTime(checkOutTime);
        attendance.setTotalHours(totalHours);
        
        AttendanceEntity saved = attendanceRepository.save(attendance);
        log.info("Check-out successful: attendanceId={}, totalHours={}", saved.getId(), totalHours);
        
        return mapToResponse(saved);
    }

    /**
     * Request leave in advance
     * - Status: LEAVE, pending approval
     */
    @Transactional
    public AttendanceResponse requestLeave(LeaveRequest request) {
        log.info("Processing leave request for intern: {}, date: {}", request.internProfileId(), request.leaveDate());
        
        // Validate intern exists
        InternProfileEntity intern = internProfileRepository.findByIdAndIsActiveTrue(request.internProfileId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.INTERN_PROFILE_NOT_FOUND));
        
        // Prevent duplicate requests
        if (attendanceRepository.existsByInternAndDate(intern.getId(), request.leaveDate())) {
            throw new ClientSideException(ErrorCode.LEAVE_ALREADY_REQUESTED);
        }
        
        // Create leave record
        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setInternProfileId(intern.getId());
        attendance.setDate(request.leaveDate());
        attendance.setStatus(AttendanceStatus.LEAVE);
        attendance.setNotes(request.reason());
        attendance.setIsActive(true);
        
        AttendanceEntity saved = attendanceRepository.save(attendance);
        log.info("Leave request created: attendanceId={}", saved.getId());
        
        return mapToResponse(saved);
    }

    /**
     * Get attendance by ID
     */
    @Transactional(readOnly = true)
    public AttendanceResponse getById(Long id) {
        AttendanceEntity attendance = attendanceRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.ATTENDANCE_NOT_FOUND));
        return mapToResponse(attendance);
    }

    /**
     * Get all attendance records for an intern (paginated)
     */
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getByInternProfile(Long internProfileId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<AttendanceEntity> attendances = attendanceRepository.findByInternProfileId(internProfileId, pageable);
        return attendances.map(this::mapToResponse);
    }

    /**
     * Get attendance statistics for an intern in a date range
     * - Total days, present/late/absent/leave counts
     * - Total hours worked
     * - Attendance rate (%)
     */
    @Transactional(readOnly = true)
    public AttendanceStatisticsResponse getStatistics(Long internProfileId, LocalDate startDate, LocalDate endDate) {
        log.info("Calculating statistics for intern: {} from {} to {}", internProfileId, startDate, endDate);
        
        // Validate intern exists
        InternProfileEntity intern = internProfileRepository.findByIdAndIsActiveTrue(internProfileId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.INTERN_PROFILE_NOT_FOUND));
        
        // Get all attendance records in range
        List<AttendanceEntity> attendances = attendanceRepository.findByInternAndDateRange(intern.getId(), startDate, endDate);
        
        // Count by status
        long presentDays = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();
        
        long lateDays = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();
        
        long absentDays = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();
        
        long leaveDays = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LEAVE)
                .count();
        
        // Calculate total hours
        BigDecimal totalHours = attendances.stream()
                .map(AttendanceEntity::getTotalHours)
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Total days recorded
        long totalDays = attendances.size();
        
        // Calculate attendance rate: (present + late) / total days * 100
        BigDecimal attendanceRate = BigDecimal.ZERO;
        if (totalDays > 0) {
            BigDecimal workingDays = BigDecimal.valueOf(presentDays + lateDays);
            attendanceRate = workingDays
                    .divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        
        return new AttendanceStatisticsResponse(
            internProfileId,
            (int) totalDays,
            (int) presentDays,
            (int) lateDays,
            (int) absentDays,
            (int) leaveDays,
            totalHours,
            attendanceRate.doubleValue()
        );
    }

    /**
     * Get monthly report for an intern
     */
    @Transactional(readOnly = true)
    public AttendanceStatisticsResponse getMonthlyReport(Long internProfileId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return getStatistics(internProfileId, startDate, endDate);
    }

    /**
     * Approve leave request (mentor only)
     */
    @Transactional
    public AttendanceResponse approveLeave(Long attendanceId, Long approverId) {
        log.info("Approving leave for attendance: {} by user: {}", attendanceId, approverId);
        
        AttendanceEntity attendance = attendanceRepository.findByIdAndIsActiveTrue(attendanceId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.ATTENDANCE_NOT_FOUND));
        
        // Validate it's a leave request
        if (attendance.getStatus() != AttendanceStatus.LEAVE) {
            throw new ClientSideException(ErrorCode.VALIDATION_ERROR);
        }
        
        attendance.setApprovedBy(approverId);
        AttendanceEntity saved = attendanceRepository.save(attendance);
        
        log.info("Leave approved successfully: attendanceId={}", saved.getId());
        return mapToResponse(saved);
    }

    /**
     * Mark attendance as ABSENT (admin/mentor action)
     */
    @Transactional
    public AttendanceResponse markAbsent(Long internProfileId, LocalDate date) {
        log.info("Marking absent for intern: {} on date: {}", internProfileId, date);
        
        // Validate intern exists
        InternProfileEntity intern = internProfileRepository.findByIdAndIsActiveTrue(internProfileId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.INTERN_PROFILE_NOT_FOUND));
        
        // Check if already exists
        if (attendanceRepository.existsByInternAndDate(intern.getId(), date)) {
            throw new ClientSideException(ErrorCode.DUPLICATE_CHECK_IN);
        }
        
        // Create absent record
        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setInternProfileId(intern.getId());
        attendance.setDate(date);
        attendance.setStatus(AttendanceStatus.ABSENT);
        attendance.setIsActive(true);
        
        AttendanceEntity saved = attendanceRepository.save(attendance);
        log.info("Marked as absent: attendanceId={}", saved.getId());
        
        return mapToResponse(saved);
    }

    /**
     * Map entity to response DTO
     */
    private AttendanceResponse mapToResponse(AttendanceEntity entity) {
        return new AttendanceResponse(
            entity.getId(),
            entity.getInternProfileId(),
            entity.getDate(),
            entity.getCheckInTime(),
            entity.getCheckOutTime(),
            entity.getStatus() != null ? entity.getStatus().name() : null,
            entity.getTotalHours(),
            entity.getNotes(),
            entity.getApprovedBy(),
            entity.getIsActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
