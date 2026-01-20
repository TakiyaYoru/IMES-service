package com.imes.core.service;

import com.imes.common.dto.request.CreateMentorAssignmentRequest;
import com.imes.common.dto.request.UpdateMentorAssignmentRequest;
import com.imes.common.dto.response.MentorAssignmentResponse;
import com.imes.core.exception.ClientSideException;
import com.imes.core.exception.ErrorCode;
import com.imes.infra.entity.AssignmentStatus;
import com.imes.infra.entity.MentorAssignmentEntity;
import com.imes.infra.repository.MentorAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MentorAssignmentService {

    private final MentorAssignmentRepository mentorAssignmentRepository;

    /**
     * Create a new mentor assignment
     */
    public MentorAssignmentResponse createAssignment(CreateMentorAssignmentRequest request) {
        log.info("Creating mentor assignment for mentor {} and intern {}", request.mentorId(), request.internProfileId());

        // Validate mentor and intern exist
        if (request.mentorId() == null || request.mentorId() <= 0) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Invalid mentor ID");
        }
        if (request.internProfileId() == null || request.internProfileId() <= 0) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Invalid intern profile ID");
        }

        // Check if assignment already exists
        if (mentorAssignmentRepository.existsActiveAssignment(request.mentorId(), request.internProfileId())) {
            throw new ClientSideException(ErrorCode.EMAIL_ALREADY_EXISTS, "Mentor already has active assignment with this intern");
        }

        MentorAssignmentEntity entity = MentorAssignmentEntity.builder()
                .mentorId(request.mentorId())
                .internProfileId(request.internProfileId())
                .assignmentStatus(AssignmentStatus.PENDING)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .isActive(true)
                .build();

        MentorAssignmentEntity saved = mentorAssignmentRepository.save(entity);
        log.info("Created mentor assignment with ID {}", saved.getId());
        return mapToResponse(saved);
    }

    /**
     * Get assignment by ID
     */
    @Transactional(readOnly = true)
    public MentorAssignmentResponse getById(Long id) {
        return mentorAssignmentRepository.findByIdAndIsActiveTrue(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Mentor assignment not found"));
    }

    /**
     * Get all assignments (paginated)
     */
    @Transactional(readOnly = true)
    public Page<MentorAssignmentResponse> getAll(Pageable pageable) {
        return mentorAssignmentRepository.findAllByIsActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get assignments by mentor ID
     */
    @Transactional(readOnly = true)
    public Page<MentorAssignmentResponse> getByMentorId(Long mentorId, Pageable pageable) {
        return mentorAssignmentRepository.findByMentorIdAndIsActiveTrue(mentorId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get assignments by intern profile ID
     */
    @Transactional(readOnly = true)
    public Page<MentorAssignmentResponse> getByInternProfileId(Long internProfileId, Pageable pageable) {
        return mentorAssignmentRepository.findByInternProfileIdAndIsActiveTrue(internProfileId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Search assignments by keyword
     */
    @Transactional(readOnly = true)
    public Page<MentorAssignmentResponse> search(String keyword, Pageable pageable) {
        return mentorAssignmentRepository.searchByKeyword(keyword, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get assignments by status
     */
    @Transactional(readOnly = true)
    public Page<MentorAssignmentResponse> getByStatus(String status, Pageable pageable) {
        try {
            AssignmentStatus assignmentStatus = AssignmentStatus.valueOf(status.toUpperCase());
            return mentorAssignmentRepository.findByStatusAndIsActiveTrue(assignmentStatus, pageable)
                    .map(this::mapToResponse);
        } catch (IllegalArgumentException e) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Invalid status: " + status);
        }
    }

    /**
     * Update assignment
     */
    public MentorAssignmentResponse update(Long id, UpdateMentorAssignmentRequest request) {
        log.info("Updating mentor assignment {}", id);

        MentorAssignmentEntity entity = mentorAssignmentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Mentor assignment not found"));

        if (request.startDate() != null) {
            entity.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            entity.setEndDate(request.endDate());
        }
        if (request.assignmentStatus() != null) {
            try {
                entity.setAssignmentStatus(AssignmentStatus.valueOf(request.assignmentStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Invalid status: " + request.assignmentStatus());
            }
        }

        MentorAssignmentEntity updated = mentorAssignmentRepository.save(entity);
        return mapToResponse(updated);
    }

    /**
     * Update assignment status
     */
    public MentorAssignmentResponse updateStatus(Long id, String status) {
        log.info("Updating status for mentor assignment {}", id);

        MentorAssignmentEntity entity = mentorAssignmentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Mentor assignment not found"));

        try {
            entity.setAssignmentStatus(AssignmentStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Invalid status: " + status);
        }

        MentorAssignmentEntity updated = mentorAssignmentRepository.save(entity);
        return mapToResponse(updated);
    }

    /**
     * Update start date
     */
    public MentorAssignmentResponse updateStartDate(Long id, LocalDate startDate) {
        MentorAssignmentEntity entity = mentorAssignmentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Mentor assignment not found"));

        entity.setStartDate(startDate);
        MentorAssignmentEntity updated = mentorAssignmentRepository.save(entity);
        return mapToResponse(updated);
    }

    /**
     * Update end date
     */
    public MentorAssignmentResponse updateEndDate(Long id, LocalDate endDate) {
        MentorAssignmentEntity entity = mentorAssignmentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Mentor assignment not found"));

        entity.setEndDate(endDate);
        MentorAssignmentEntity updated = mentorAssignmentRepository.save(entity);
        return mapToResponse(updated);
    }

    /**
     * Soft delete assignment
     */
    public void delete(Long id) {
        log.info("Deleting mentor assignment {}", id);

        MentorAssignmentEntity entity = mentorAssignmentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Mentor assignment not found"));

        entity.setIsActive(false);
        mentorAssignmentRepository.save(entity);
        log.info("Deleted mentor assignment {}", id);
    }

    /**
     * Get total count of active assignments
     */
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return mentorAssignmentRepository.countByIsActiveTrue();
    }

    /**
     * Get count by status
     */
    @Transactional(readOnly = true)
    public long getCountByStatus(String status) {
        try {
            AssignmentStatus assignmentStatus = AssignmentStatus.valueOf(status.toUpperCase());
            return mentorAssignmentRepository.countByStatusAndIsActiveTrue(assignmentStatus);
        } catch (IllegalArgumentException e) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Invalid status: " + status);
        }
    }

    // Helper method to map entity to response
    private MentorAssignmentResponse mapToResponse(MentorAssignmentEntity entity) {
        return new MentorAssignmentResponse(
                entity.getId(),
                entity.getMentorId(),
                entity.getInternProfileId(),
                entity.getAssignmentStatus().name(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
