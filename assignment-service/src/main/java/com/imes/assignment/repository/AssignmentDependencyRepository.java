package com.imes.assignment.repository;

import com.imes.assignment.entity.AssignmentDependencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentDependencyRepository extends JpaRepository<AssignmentDependencyEntity, Long> {

    List<AssignmentDependencyEntity> findByAssignmentId(Long assignmentId);

    List<AssignmentDependencyEntity> findByAssignmentIdOrderByCreatedAtAsc(Long assignmentId);

    boolean existsByAssignmentIdAndDependsOnAssignmentId(Long assignmentId, Long dependsOnAssignmentId);

    long deleteByAssignmentIdAndDependsOnAssignmentId(Long assignmentId, Long dependsOnAssignmentId);
}
