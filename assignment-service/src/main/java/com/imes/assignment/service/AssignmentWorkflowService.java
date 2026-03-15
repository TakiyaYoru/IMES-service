package com.imes.assignment.service;

import com.imes.assignment.entity.AssignmentStatus;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class AssignmentWorkflowService {

    private final Map<AssignmentStatus, Set<AssignmentStatus>> transitionMap = new EnumMap<>(AssignmentStatus.class);

    public AssignmentWorkflowService() {
        transitionMap.put(AssignmentStatus.DRAFT, EnumSet.of(
                AssignmentStatus.PUBLISHED,
                AssignmentStatus.CANCELLED
        ));
        transitionMap.put(AssignmentStatus.PUBLISHED, EnumSet.of(
                AssignmentStatus.ACCEPTED,
                AssignmentStatus.REJECTED,
                AssignmentStatus.CANCELLED
        ));
        transitionMap.put(AssignmentStatus.ACCEPTED, EnumSet.of(
                AssignmentStatus.IN_PROGRESS,
                AssignmentStatus.CANCELLED
        ));
        transitionMap.put(AssignmentStatus.REJECTED, EnumSet.of(
                AssignmentStatus.CANCELLED
        ));
        transitionMap.put(AssignmentStatus.IN_PROGRESS, EnumSet.of(
                AssignmentStatus.SUBMITTED,
                AssignmentStatus.CANCELLED
        ));
        transitionMap.put(AssignmentStatus.SUBMITTED, EnumSet.of(
                AssignmentStatus.APPROVED,
                AssignmentStatus.REVISION_REQUESTED,
                AssignmentStatus.CANCELLED
        ));
        transitionMap.put(AssignmentStatus.REVISION_REQUESTED, EnumSet.of(
                AssignmentStatus.IN_PROGRESS,
                AssignmentStatus.SUBMITTED,
                AssignmentStatus.CANCELLED
        ));
        transitionMap.put(AssignmentStatus.APPROVED, EnumSet.of(
                AssignmentStatus.COMPLETED
        ));
        transitionMap.put(AssignmentStatus.COMPLETED, EnumSet.noneOf(AssignmentStatus.class));
        transitionMap.put(AssignmentStatus.CANCELLED, EnumSet.noneOf(AssignmentStatus.class));

        // Legacy compatibility
        transitionMap.put(AssignmentStatus.OPEN, EnumSet.of(
                AssignmentStatus.PUBLISHED,
                AssignmentStatus.SUBMITTED,
                AssignmentStatus.COMPLETED,
                AssignmentStatus.CANCELLED
        ));
    }

    public AssignmentStatus transitionOrThrow(AssignmentStatus currentStatus, AssignmentStatus targetStatus) {
        Set<AssignmentStatus> allowed = transitionMap.getOrDefault(currentStatus, EnumSet.noneOf(AssignmentStatus.class));
        if (!allowed.contains(targetStatus)) {
            throw new IllegalStateException(
                    String.format("Invalid assignment status transition: %s -> %s", currentStatus, targetStatus)
            );
        }
        return targetStatus;
    }
}
