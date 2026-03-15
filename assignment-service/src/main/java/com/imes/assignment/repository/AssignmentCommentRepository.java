package com.imes.assignment.repository;

import com.imes.assignment.entity.AssignmentComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentCommentRepository extends JpaRepository<AssignmentComment, Long> {

    List<AssignmentComment> findByAssignmentIdOrderByCreatedAtAsc(Long assignmentId);
}
