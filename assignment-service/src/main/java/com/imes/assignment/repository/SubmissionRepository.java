package com.imes.assignment.repository;

import com.imes.assignment.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignmentId(Long assignmentId);
    List<Submission> findByInternId(Long internId);
    boolean existsByAssignmentIdAndInternId(Long assignmentId, Long internId);
    long countByAssignmentId(Long assignmentId);
}
