package com.imes.assignment.repository;

import com.imes.assignment.entity.AssignmentChecklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentChecklistRepository extends JpaRepository<AssignmentChecklistEntity, Long> {

    List<AssignmentChecklistEntity> findByAssignmentIdOrderByDisplayOrderAscIdAsc(Long assignmentId);

    Optional<AssignmentChecklistEntity> findTopByAssignmentIdOrderByDisplayOrderDescIdDesc(Long assignmentId);

    long countByAssignmentId(Long assignmentId);

    long countByAssignmentIdAndIsCompletedTrue(Long assignmentId);
}
