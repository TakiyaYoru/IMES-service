package com.imes.assignment.repository;

import com.imes.assignment.entity.AssignmentTimeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface AssignmentTimeLogRepository extends JpaRepository<AssignmentTimeLogEntity, Long> {

    List<AssignmentTimeLogEntity> findByAssignmentIdOrderByWorkDateDescCreatedAtDesc(Long assignmentId);

    List<AssignmentTimeLogEntity> findByInternIdOrderByWorkDateDescCreatedAtDesc(Long internId);

    @Query("select coalesce(sum(t.hoursSpent), 0) from AssignmentTimeLogEntity t where t.assignmentId = :assignmentId")
    BigDecimal sumHoursByAssignmentId(Long assignmentId);
}
