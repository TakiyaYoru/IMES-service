package com.imes.infra.repository;

import com.imes.infra.entity.AssignmentStatus;
import com.imes.infra.entity.MentorAssignmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorAssignmentRepository extends JpaRepository<MentorAssignmentEntity, Long> {

    // Find by ID and active
    Optional<MentorAssignmentEntity> findByIdAndIsActiveTrue(Long id);

    // Find all active assignments
    Page<MentorAssignmentEntity> findAllByIsActiveTrue(Pageable pageable);

    // Find by mentor ID
    @Query("SELECT m FROM MentorAssignmentEntity m WHERE m.mentorId = :mentorId AND m.isActive = true")
    Page<MentorAssignmentEntity> findByMentorIdAndIsActiveTrue(@Param("mentorId") Long mentorId, Pageable pageable);

    // Find by intern profile ID
    @Query("SELECT m FROM MentorAssignmentEntity m WHERE m.internProfileId = :internProfileId AND m.isActive = true")
    Page<MentorAssignmentEntity> findByInternProfileIdAndIsActiveTrue(@Param("internProfileId") Long internProfileId, Pageable pageable);

    // Find by status
    @Query("SELECT m FROM MentorAssignmentEntity m WHERE m.assignmentStatus = :status AND m.isActive = true")
    Page<MentorAssignmentEntity> findByStatusAndIsActiveTrue(@Param("status") AssignmentStatus status, Pageable pageable);

    // Search by keyword (mentor or intern name/email)
    @Query("SELECT m FROM MentorAssignmentEntity m WHERE m.isActive = true AND " +
           "(LOWER(CAST(m.mentorId AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(CAST(m.internProfileId AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<MentorAssignmentEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Check if mentor has active assignment with intern
    @Query("SELECT COUNT(m) > 0 FROM MentorAssignmentEntity m WHERE m.mentorId = :mentorId " +
           "AND m.internProfileId = :internProfileId AND m.isActive = true")
    boolean existsActiveAssignment(@Param("mentorId") Long mentorId, @Param("internProfileId") Long internProfileId);

    // Count by status
    @Query("SELECT COUNT(m) FROM MentorAssignmentEntity m WHERE m.assignmentStatus = :status AND m.isActive = true")
    long countByStatusAndIsActiveTrue(@Param("status") AssignmentStatus status);
    
    // Count active interns assigned to mentor
    @Query("SELECT COUNT(m) FROM MentorAssignmentEntity m WHERE m.mentorId = :mentorId AND m.isActive = true")
    long countActiveInternsByMentorId(@Param("mentorId") Long mentorId);

    // Count total active
    long countByIsActiveTrue();
}
