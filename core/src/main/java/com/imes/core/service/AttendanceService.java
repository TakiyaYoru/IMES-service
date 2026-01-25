package com.imes.core.service;

import com.imes.common.dto.request.CheckInRequest;
import com.imes.common.dto.request.CheckOutRequest;
import com.imes.common.dto.request.LeaveRequest;
import com.imes.common.dto.response.AttendanceResponse;
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
