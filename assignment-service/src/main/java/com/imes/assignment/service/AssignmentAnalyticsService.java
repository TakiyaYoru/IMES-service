package com.imes.assignment.service;

import com.imes.assignment.dto.*;
import com.imes.assignment.entity.Assignment;
import com.imes.assignment.entity.AssignmentStatus;
import com.imes.assignment.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentAnalyticsService {

    private static final Set<AssignmentStatus> ACTIVE_STATUSES = Set.of(
            AssignmentStatus.PUBLISHED,
            AssignmentStatus.ACCEPTED,
            AssignmentStatus.IN_PROGRESS,
            AssignmentStatus.SUBMITTED,
            AssignmentStatus.REVISION_REQUESTED,
            AssignmentStatus.APPROVED,
            AssignmentStatus.OPEN,
            AssignmentStatus.DRAFT
    );

    private final AssignmentRepository assignmentRepository;

    public CompletionRateResponse getCompletionRate() {
        List<Assignment> assignments = assignmentRepository.findAll();
        long totalAssignments = assignments.size();
        long completedAssignments = assignments.stream()
                .filter(assignment -> assignment.getStatus() == AssignmentStatus.COMPLETED)
                .count();

        return CompletionRateResponse.builder()
                .totalAssignments(totalAssignments)
                .completedAssignments(completedAssignments)
                .completionRate(calculatePercentage(completedAssignments, totalAssignments))
                .build();
    }

    public TimeEstimationAccuracyResponse getTimeEstimationAccuracy() {
        List<Assignment> completedAssignments = assignmentRepository.findAll().stream()
                .filter(assignment -> assignment.getStatus() == AssignmentStatus.COMPLETED)
                .toList();

        long totalCompleted = completedAssignments.size();
        long onTimeCompleted = completedAssignments.stream()
                .filter(this::isCompletedOnTime)
                .count();

        return TimeEstimationAccuracyResponse.builder()
                .totalCompletedAssignments(totalCompleted)
                .onTimeCompletedAssignments(onTimeCompleted)
                .lateCompletedAssignments(totalCompleted - onTimeCompleted)
                .accuracyPercentage(calculatePercentage(onTimeCompleted, totalCompleted))
                .build();
    }

    public OverdueSummaryResponse getOverdueSummary() {
        List<Assignment> assignments = assignmentRepository.findAll();
        long totalAssignments = assignments.size();
        long overdueAssignments = assignments.stream()
                .filter(this::isOverdue)
                .count();

        return OverdueSummaryResponse.builder()
                .totalAssignments(totalAssignments)
                .overdueAssignments(overdueAssignments)
                .overduePercentage(calculatePercentage(overdueAssignments, totalAssignments))
                .build();
    }

    public WorkloadDistributionResponse getWorkloadDistribution() {
        List<Assignment> assignments = assignmentRepository.findAll();

        Map<Long, List<Assignment>> byMentor = assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getMentorId));

        List<WorkloadDistributionItemResponse> items = byMentor.entrySet().stream()
                .map(entry -> {
                    List<Assignment> mentorAssignments = entry.getValue();
                    long activeAssignments = mentorAssignments.stream()
                            .filter(assignment -> ACTIVE_STATUSES.contains(assignment.getStatus()))
                            .count();
                    long completedAssignments = mentorAssignments.stream()
                            .filter(assignment -> assignment.getStatus() == AssignmentStatus.COMPLETED)
                            .count();
                    long overdueAssignments = mentorAssignments.stream()
                            .filter(this::isOverdue)
                            .count();

                    return WorkloadDistributionItemResponse.builder()
                            .mentorId(entry.getKey())
                            .totalAssignments(mentorAssignments.size())
                            .activeAssignments(activeAssignments)
                            .completedAssignments(completedAssignments)
                            .overdueAssignments(overdueAssignments)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.totalAssignments(), a.totalAssignments()))
                .toList();

        long totalActiveAssignments = assignments.stream()
                .filter(assignment -> ACTIVE_STATUSES.contains(assignment.getStatus()))
                .count();

        return WorkloadDistributionResponse.builder()
                .items(items)
                .totalMentors(items.size())
                .totalActiveAssignments(totalActiveAssignments)
                .build();
    }

    private boolean isOverdue(Assignment assignment) {
        return assignment.getDeadline() != null
                && assignment.getDeadline().isBefore(LocalDate.now())
                && assignment.getStatus() != AssignmentStatus.COMPLETED
                && assignment.getStatus() != AssignmentStatus.CANCELLED;
    }

    private boolean isCompletedOnTime(Assignment assignment) {
        if (assignment.getUpdatedAt() == null || assignment.getDeadline() == null) {
            return false;
        }
        LocalDateTime deadlineEndOfDay = assignment.getDeadline().atTime(23, 59, 59);
        return !assignment.getUpdatedAt().isAfter(deadlineEndOfDay);
    }

    private double calculatePercentage(long numerator, long denominator) {
        if (denominator == 0) {
            return 0.0;
        }
        return Math.round(((double) numerator * 10000.0) / denominator) / 100.0;
    }
}
