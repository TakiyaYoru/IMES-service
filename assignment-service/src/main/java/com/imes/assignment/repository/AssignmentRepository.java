package com.imes.assignment.repository;

import com.imes.assignment.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Page<Assignment> findByMentorId(Long mentorId, Pageable pageable);
    long countByMentorId(Long mentorId);
    Page<Assignment> findByIdIn(List<Long> ids, Pageable pageable);
}
