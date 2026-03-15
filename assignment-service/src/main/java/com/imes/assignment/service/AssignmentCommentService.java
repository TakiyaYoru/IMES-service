package com.imes.assignment.service;

import com.imes.assignment.dto.AssignmentCommentResponse;
import com.imes.assignment.dto.CreateCommentRequest;
import com.imes.assignment.dto.UpdateCommentRequest;
import com.imes.assignment.entity.Assignment;
import com.imes.assignment.entity.AssignmentComment;
import com.imes.assignment.repository.AssignmentCommentRepository;
import com.imes.assignment.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentCommentService {

    private final AssignmentCommentRepository assignmentCommentRepository;
    private final AssignmentRepository assignmentRepository;

    @Transactional
    public AssignmentCommentResponse createComment(Long assignmentId, CreateCommentRequest request, Long userId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        AssignmentComment comment = AssignmentComment.builder()
                .assignmentId(assignment.getId())
                .userId(userId)
                .comment(request.comment().trim())
                .build();

        AssignmentComment saved = assignmentCommentRepository.save(comment);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AssignmentCommentResponse> getComments(Long assignmentId) {
        assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        return assignmentCommentRepository.findByAssignmentIdOrderByCreatedAtAsc(assignmentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AssignmentCommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long userId) {
        AssignmentComment comment = assignmentCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("You are not authorized to edit this comment");
        }

        comment.setComment(request.comment().trim());
        AssignmentComment saved = assignmentCommentRepository.saveAndFlush(comment);
        return toResponse(saved);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        AssignmentComment comment = assignmentCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("You are not authorized to delete this comment");
        }

        assignmentCommentRepository.delete(comment);
    }

    private AssignmentCommentResponse toResponse(AssignmentComment comment) {
        boolean edited = comment.getUpdatedAt() != null;
        return AssignmentCommentResponse.builder()
                .id(comment.getId())
                .assignmentId(comment.getAssignmentId())
                .userId(comment.getUserId())
                .comment(comment.getComment())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .edited(edited)
                .build();
    }
}
