package com.imes.assignment.controller;

import com.imes.assignment.dto.AssignmentCommentResponse;
import com.imes.assignment.dto.CreateCommentRequest;
import com.imes.assignment.dto.UpdateCommentRequest;
import com.imes.assignment.service.AssignmentCommentService;
import com.imes.common.dto.ResponseApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
@Slf4j
public class AssignmentCommentController {

    private final AssignmentCommentService assignmentCommentService;

    @PostMapping("/{id}/comments")
    public ResponseApi<AssignmentCommentResponse> createComment(
            @PathVariable("id") Long assignmentId,
            @Valid @RequestBody CreateCommentRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "3") Long userId
    ) {
        log.info("Creating comment for assignment {} by user {}", assignmentId, userId);
        AssignmentCommentResponse response = assignmentCommentService.createComment(assignmentId, request, userId);
        return ResponseApi.success(response);
    }

    @GetMapping("/{id}/comments")
    public ResponseApi<List<AssignmentCommentResponse>> getComments(@PathVariable("id") Long assignmentId) {
        List<AssignmentCommentResponse> comments = assignmentCommentService.getComments(assignmentId);
        return ResponseApi.success(comments);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseApi<AssignmentCommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "3") Long userId
    ) {
        AssignmentCommentResponse response = assignmentCommentService.updateComment(commentId, request, userId);
        return ResponseApi.success(response);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseApi<String> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "3") Long userId
    ) {
        assignmentCommentService.deleteComment(commentId, userId);
        return ResponseApi.success("Comment deleted successfully");
    }
}
