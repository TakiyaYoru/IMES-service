package com.imes.assignment.service;

import com.imes.assignment.dto.*;
import com.imes.assignment.entity.Assignment;
import com.imes.assignment.entity.AssignmentStatus;
import com.imes.assignment.entity.Submission;
import com.imes.assignment.repository.AssignmentRepository;
import com.imes.assignment.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("assignmentManagementService")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentWorkflowService assignmentWorkflowService;
    private final AssignmentDependencyService assignmentDependencyService;

    @Transactional
    public AssignmentResponse createAssignment(CreateAssignmentRequest request, Long mentorId) {
        log.info("Creating assignment for mentor: {}", mentorId);

        Assignment assignment = Assignment.builder()
                .title(request.title())
                .description(request.description())
                .deadline(request.deadline())
                .mentorId(mentorId)
                .status(AssignmentStatus.OPEN)
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        log.info("Created assignment ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    public Page<AssignmentResponse> getAssignmentsByMentor(Long mentorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Assignment> assignments = assignmentRepository.findByMentorId(mentorId, pageable);
        return assignments.map(this::mapToResponse);
    }

    public AssignmentResponse getAssignmentById(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));
        return mapToResponse(assignment);
    }

    @Transactional
    public AssignmentResponse updateAssignment(Long assignmentId, UpdateAssignmentRequest request, Long mentorId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        if (!assignment.getMentorId().equals(mentorId)) {
            throw new RuntimeException("You are not authorized to update this assignment");
        }

        if (request.title() != null && !request.title().isBlank()) {
            assignment.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            assignment.setDescription(request.description().trim());
        }
        if (request.deadline() != null) {
            assignment.setDeadline(request.deadline());
        }
        if (request.status() != null) {
            assignment.setStatus(
                    assignmentWorkflowService.transitionOrThrow(assignment.getStatus(), request.status())
            );
        }

        Assignment updated = assignmentRepository.save(assignment);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteAssignment(Long assignmentId, Long mentorId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        if (!assignment.getMentorId().equals(mentorId)) {
            throw new RuntimeException("You are not authorized to delete this assignment");
        }

        if (submissionRepository.countByAssignmentId(assignmentId) > 0) {
            throw new RuntimeException("Cannot delete assignment that already has submissions");
        }

        assignmentRepository.delete(assignment);
    }

    @Transactional
    public SubmissionResponse submitAssignment(Long assignmentId, SubmitAssignmentRequest request, Long internId) {
        log.info("Intern {} submitting assignment {}", internId, assignmentId);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        assignment.setStatus(
                assignmentWorkflowService.transitionOrThrow(assignment.getStatus(), AssignmentStatus.SUBMITTED)
        );

        if (submissionRepository.existsByAssignmentIdAndInternId(assignmentId, internId)) {
            throw new RuntimeException("You have already submitted this assignment");
        }

        Submission submission = Submission.builder()
                .assignmentId(assignmentId)
                .internId(internId)
                .content(request.content())
                .attachmentUrl(request.attachmentUrl())
                .build();

        if (LocalDateTime.now().isAfter(assignment.getDeadline().atTime(23, 59, 59))) {
            submission.setIsLate(true);
            log.warn("Late submission detected for assignment {} by intern {}", assignmentId, internId);
        }

        Submission saved = submissionRepository.save(submission);
        assignmentRepository.save(assignment);

        log.info("Created submission ID: {}", saved.getId());
        return mapToSubmissionResponse(saved);
    }

    public List<SubmissionResponse> getSubmissionsByAssignment(Long assignmentId) {
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        return submissions.stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssignmentResponse markAsCompleted(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        assignmentDependencyService.validateCanComplete(assignmentId);

        assignment.setStatus(
                assignmentWorkflowService.transitionOrThrow(assignment.getStatus(), AssignmentStatus.COMPLETED)
        );
        Assignment updated = assignmentRepository.save(assignment);
        return mapToResponse(updated);
    }

    @Transactional
    public SubmissionResponse reviewSubmission(Long submissionId, ReviewSubmissionRequest request, Long mentorId) {
        log.info("Mentor {} reviewing submission {}", mentorId, submissionId);

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found: " + submissionId));

        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!assignment.getMentorId().equals(mentorId)) {
            throw new RuntimeException("You are not authorized to review this submission");
        }

        submission.setScore(request.score());
        submission.setMentorComments(request.comments());
        submission.setReviewedAt(LocalDateTime.now());
        submission.setReviewedBy(mentorId);

        Submission updated = submissionRepository.save(submission);

        AssignmentStatus targetStatus = request.score() >= 5.0
                ? AssignmentStatus.APPROVED
                : AssignmentStatus.REVISION_REQUESTED;
        assignment.setStatus(
                assignmentWorkflowService.transitionOrThrow(assignment.getStatus(), targetStatus)
        );
        assignmentRepository.save(assignment);

        log.info("Submission {} reviewed with score {}", submissionId, request.score());
        return mapToSubmissionResponse(updated);
    }

    @Transactional
    public AssignmentResponse publishAssignment(Long assignmentId) {
        return transitionAssignmentStatus(assignmentId, AssignmentStatus.PUBLISHED);
    }

    @Transactional
    public AssignmentResponse acceptAssignment(Long assignmentId) {
        return transitionAssignmentStatus(assignmentId, AssignmentStatus.ACCEPTED);
    }

    @Transactional
    public AssignmentResponse rejectAssignment(Long assignmentId) {
        return transitionAssignmentStatus(assignmentId, AssignmentStatus.REJECTED);
    }

    @Transactional
    public AssignmentResponse startAssignment(Long assignmentId) {
        return transitionAssignmentStatus(assignmentId, AssignmentStatus.IN_PROGRESS);
    }

    @Transactional
    public AssignmentResponse submitWorkAssignment(Long assignmentId) {
        return transitionAssignmentStatus(assignmentId, AssignmentStatus.SUBMITTED);
    }

    @Transactional
    public AssignmentResponse requestRevision(Long assignmentId) {
        return transitionAssignmentStatus(assignmentId, AssignmentStatus.REVISION_REQUESTED);
    }

    @Transactional
    public AssignmentResponse approveAssignment(Long assignmentId) {
        return transitionAssignmentStatus(assignmentId, AssignmentStatus.APPROVED);
    }

    @Transactional
    public AssignmentResponse cancelAssignment(Long assignmentId) {
        return transitionAssignmentStatus(assignmentId, AssignmentStatus.CANCELLED);
    }

    @Transactional
    public List<AssignmentResponse> bulkUpdateStatus(BulkUpdateAssignmentStatusRequest request) {
        List<AssignmentResponse> results = new ArrayList<>();
        for (Long assignmentId : request.assignmentIds()) {
            results.add(transitionAssignmentStatus(assignmentId, request.targetStatus()));
        }
        return results;
    }

    /**
     * Get all assignments that an intern has submitted work for.
     */
    public Page<AssignmentResponse> getAssignmentsByIntern(Long internId, int page, int size) {
        List<Long> assignmentIds = submissionRepository.findByInternId(internId)
                .stream()
                .map(Submission::getAssignmentId)
                .distinct()
                .collect(Collectors.toList());

        if (assignmentIds.isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return assignmentRepository.findByIdIn(assignmentIds, pageable).map(this::mapToResponse);
    }

    private AssignmentResponse transitionAssignmentStatus(Long assignmentId, AssignmentStatus targetStatus) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        if (targetStatus == AssignmentStatus.COMPLETED) {
            assignmentDependencyService.validateCanComplete(assignmentId);
        }

        assignment.setStatus(
                assignmentWorkflowService.transitionOrThrow(assignment.getStatus(), targetStatus)
        );

        Assignment updated = assignmentRepository.save(assignment);
        return mapToResponse(updated);
    }

    private AssignmentResponse mapToResponse(Assignment assignment) {
        int submissionCount = (int) submissionRepository.countByAssignmentId(assignment.getId());

        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .deadline(assignment.getDeadline())
                .mentorId(assignment.getMentorId())
            .mentorName("Mentor #" + assignment.getMentorId())
                .status(assignment.getStatus())
                .submissionCount(submissionCount)
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }

    private SubmissionResponse mapToSubmissionResponse(Submission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignmentId())
                .assignmentTitle(resolveAssignmentTitle(submission.getAssignmentId()))
                .internId(submission.getInternId())
                .internName("Intern #" + submission.getInternId())
                .content(submission.getContent())
                .attachmentUrl(submission.getAttachmentUrl())
                .submittedAt(submission.getSubmittedAt())
                .isLate(submission.getIsLate())
                .score(submission.getScore())
                .mentorComments(submission.getMentorComments())
                .reviewedAt(submission.getReviewedAt())
                .reviewedBy(submission.getReviewedBy())
                .build();
    }

    private String resolveAssignmentTitle(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .map(Assignment::getTitle)
                .orElse("Assignment #" + assignmentId);
    }
}
