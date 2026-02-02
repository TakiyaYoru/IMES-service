package com.imes.core.service;

import com.imes.common.dto.request.CreateAssignmentRequest;
import com.imes.common.dto.request.UpdateAssignmentRequest;
import com.imes.common.dto.response.AssignmentResponse;
import com.imes.core.exception.ClientSideException;
import com.imes.core.exception.ErrorCode;
import com.imes.infra.entity.AssignmentEntity;
import com.imes.infra.entity.UserEntity;
import com.imes.infra.repository.AssignmentRepository;
import com.imes.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Assignment management
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    /**
     * Create a new assignment
     */
    public AssignmentResponse createAssignment(CreateAssignmentRequest request, Long mentorId) {
        log.info("Creating assignment: {} by mentor: {}", request.title(), mentorId);

        // Validate deadline is in the future
        if (request.deadline().isBefore(LocalDateTime.now())) {
            throw new ClientSideException(ErrorCode.TASK_INVALID_DEADLINE, 
                "Deadline must be in the future");
        }

        // Validate mentor exists
        UserEntity mentor = userRepository.findById(mentorId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.USER_NOT_FOUND, 
                "Mentor not found"));

        AssignmentEntity entity = AssignmentEntity.builder()
            .title(request.title())
            .description(request.description())
            .deadline(request.deadline())
            .createdBy(mentorId)
            .isActive(true)
            .build();

        AssignmentEntity saved = assignmentRepository.save(entity);
        log.info("Created assignment with ID: {}", saved.getId());
        
        return mapToResponse(saved, mentor.getFullName());
    }

    /**
     * Get assignment by ID
     */
    public AssignmentResponse getAssignmentById(Long id) {
        log.info("Getting assignment by ID: {}", id);
        
        AssignmentEntity entity = assignmentRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ClientSideException(ErrorCode.TASK_NOT_FOUND, 
                "Assignment not found"));

        String mentorName = getUserFullName(entity.getCreatedBy());
        return mapToResponse(entity, mentorName);
    }

    /**
     * Update assignment
     */
    public AssignmentResponse updateAssignment(Long id, UpdateAssignmentRequest request, Long mentorId) {
        log.info("Updating assignment ID: {} by mentor: {}", id, mentorId);

        AssignmentEntity entity = assignmentRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ClientSideException(ErrorCode.TASK_NOT_FOUND, 
                "Assignment not found"));

        // Check ownership
        if (!entity.getCreatedBy().equals(mentorId)) {
            throw new ClientSideException(ErrorCode.TASK_UNAUTHORIZED, 
                "You are not authorized to update this assignment");
        }

        // Update fields if provided
        if (request.title() != null && !request.title().isBlank()) {
            entity.setTitle(request.title());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.deadline() != null) {
            if (request.deadline().isBefore(LocalDateTime.now())) {
                throw new ClientSideException(ErrorCode.TASK_INVALID_DEADLINE, 
                    "Deadline must be in the future");
            }
            entity.setDeadline(request.deadline());
        }

        AssignmentEntity updated = assignmentRepository.save(entity);
        log.info("Updated assignment ID: {}", id);

        String mentorName = getUserFullName(mentorId);
        return mapToResponse(updated, mentorName);
    }

    /**
     * Delete assignment (soft delete)
     */
    public void deleteAssignment(Long id, Long mentorId) {
        log.info("Deleting assignment ID: {} by mentor: {}", id, mentorId);

        AssignmentEntity entity = assignmentRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ClientSideException(ErrorCode.TASK_NOT_FOUND, 
                "Assignment not found"));

        // Check ownership
        if (!entity.getCreatedBy().equals(mentorId)) {
            throw new ClientSideException(ErrorCode.TASK_UNAUTHORIZED, 
                "You are not authorized to delete this assignment");
        }

        entity.setIsActive(false);
        assignmentRepository.save(entity);
        log.info("Deleted assignment ID: {}", id);
    }

    /**
     * Get all assignments (paginated)
     */
    public Page<AssignmentResponse> getAllAssignments(int page, int size) {
        log.info("Getting all assignments - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AssignmentEntity> entities = assignmentRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        
        return entities.map(entity -> {
            String mentorName = getUserFullName(entity.getCreatedBy());
            return mapToResponse(entity, mentorName);
        });
    }

    /**
     * Get assignments by mentor
     */
    public List<AssignmentResponse> getAssignmentsByMentor(Long mentorId) {
        log.info("Getting assignments by mentor: {}", mentorId);

        // Validate mentor exists
        UserEntity mentor = userRepository.findById(mentorId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.USER_NOT_FOUND, 
                "Mentor not found"));

        List<AssignmentEntity> entities = assignmentRepository
            .findByCreatedByAndIsActiveTrueOrderByDeadlineAsc(mentorId);

        return entities.stream()
            .map(entity -> mapToResponse(entity, mentor.getFullName()))
            .toList();
    }

    /**
     * Map entity to response DTO
     */
    private AssignmentResponse mapToResponse(AssignmentEntity entity, String mentorName) {
        return new AssignmentResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getDeadline(),
            entity.getCreatedBy(),
            mentorName,
            entity.getIsActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * Get user full name by ID
     */
    private String getUserFullName(Long userId) {
        return userRepository.findById(userId)
            .map(UserEntity::getFullName)
            .orElse("Unknown");
    }
}
