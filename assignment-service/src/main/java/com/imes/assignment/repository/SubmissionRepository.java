package com.imes.assignment.repository;

import com.imes.assignment.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    List<Submission> findByAssignmentId(Long assignmentId);
    
    Optional<Submission> findByAssignmentIdAndInternId(Long assignmentId, Long internId);
    
    long countByAssignmentId(Long assignmentId);
    
    boolean existsByAssignmentIdAndInternId(Long assignmentId, Long internId);
}
