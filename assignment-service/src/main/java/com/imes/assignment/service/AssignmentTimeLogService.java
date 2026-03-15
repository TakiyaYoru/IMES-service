package com.imes.assignment.service;

import com.imes.assignment.dto.CreateTimeLogRequest;
import com.imes.assignment.dto.TimeLogListResponse;
import com.imes.assignment.dto.TimeLogResponse;
import com.imes.assignment.entity.Assignment;
import com.imes.assignment.entity.AssignmentTimeLogEntity;
import com.imes.assignment.repository.AssignmentRepository;
import com.imes.assignment.repository.AssignmentTimeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentTimeLogService {

    private final AssignmentTimeLogRepository assignmentTimeLogRepository;
    private final AssignmentRepository assignmentRepository;

    @Transactional
    public TimeLogResponse addTimeLog(Long assignmentId, CreateTimeLogRequest request, Long internId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        AssignmentTimeLogEntity entity = AssignmentTimeLogEntity.builder()
                .assignmentId(assignment.getId())
                .internId(internId)
                .hoursSpent(request.hoursSpent())
                .workDate(request.workDate())
                .description(request.description() == null ? null : request.description().trim())
                .build();

        AssignmentTimeLogEntity saved = assignmentTimeLogRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TimeLogListResponse getByAssignment(Long assignmentId) {
        assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        List<TimeLogResponse> items = assignmentTimeLogRepository
                .findByAssignmentIdOrderByWorkDateDescCreatedAtDesc(assignmentId)
                .stream()
                .map(this::toResponse)
                .toList();

        BigDecimal totalHours = assignmentTimeLogRepository.sumHoursByAssignmentId(assignmentId);

        return TimeLogListResponse.builder()
                .items(items)
                .totalHours(totalHours)
                .build();
    }

    @Transactional(readOnly = true)
    public TimeLogListResponse getByIntern(Long internId) {
        List<TimeLogResponse> items = assignmentTimeLogRepository
                .findByInternIdOrderByWorkDateDescCreatedAtDesc(internId)
                .stream()
                .map(this::toResponse)
                .toList();

        BigDecimal totalHours = items.stream()
                .map(TimeLogResponse::hoursSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return TimeLogListResponse.builder()
                .items(items)
                .totalHours(totalHours)
                .build();
    }

    private TimeLogResponse toResponse(AssignmentTimeLogEntity entity) {
        return TimeLogResponse.builder()
                .id(entity.getId())
                .assignmentId(entity.getAssignmentId())
                .internId(entity.getInternId())
                .hoursSpent(entity.getHoursSpent())
                .workDate(entity.getWorkDate())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
