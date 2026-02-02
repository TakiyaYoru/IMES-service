package com.imes.infra.repository;

import com.imes.infra.entity.AssignmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Assignment entity
 */
@Repository
public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long> {

    /**
     * Find assignment by ID and isActive = true
     */
    Optional<AssignmentEntity> findByIdAndIsActiveTrue(Long id);

    /**
     * Find all active assignments created by a mentor
     */
    List<AssignmentEntity> findByCreatedByAndIsActiveTrueOrderByDeadlineAsc(Long mentorId);

    /**
     * Find all active assignments (paginated)
     */
    Page<AssignmentEntity> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find assignments with deadline after a certain date
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE a.deadline > :date AND a.isActive = true ORDER BY a.deadline ASC")
    List<AssignmentEntity> findUpcomingAssignments(@Param("date") LocalDateTime date);

    /**
     * Find assignments with deadline before a certain date (overdue)
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE a.deadline < :date AND a.isActive = true ORDER BY a.deadline DESC")
    List<AssignmentEntity> findOverdueAssignments(@Param("date") LocalDateTime date);

    /**
     * Count active assignments created by a mentor
     */
    @Query("SELECT COUNT(a) FROM AssignmentEntity a WHERE a.createdBy = :mentorId AND a.isActive = true")
    long countActiveAssignmentsByMentor(@Param("mentorId") Long mentorId);

    /**
     * Search assignments by title (case-insensitive)
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND a.isActive = true")
    Page<AssignmentEntity> searchByTitle(@Param("keyword") String keyword, Pageable pageable);
}
