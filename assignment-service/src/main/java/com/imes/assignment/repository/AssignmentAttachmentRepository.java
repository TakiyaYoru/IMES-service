package com.imes.assignment.repository;

import com.imes.assignment.entity.AssignmentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentAttachmentRepository extends JpaRepository<AssignmentAttachment, Long> {

    List<AssignmentAttachment> findByAssignmentIdAndIsActiveTrueOrderByUploadedAtDesc(Long assignmentId);

    Optional<AssignmentAttachment> findByIdAndIsActiveTrue(Long id);
}
