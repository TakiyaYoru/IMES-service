package com.imes.assignment.service;

import com.imes.assignment.dto.AssignmentDependencyResponse;
import com.imes.assignment.entity.Assignment;
import com.imes.assignment.entity.AssignmentDependencyEntity;
import com.imes.assignment.entity.AssignmentStatus;
import com.imes.assignment.repository.AssignmentDependencyRepository;
import com.imes.assignment.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AssignmentDependencyService {

    private final AssignmentDependencyRepository assignmentDependencyRepository;
    private final AssignmentRepository assignmentRepository;

    @Transactional
    public AssignmentDependencyResponse addDependency(Long assignmentId, Long dependsOnAssignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        Assignment dependency = assignmentRepository.findById(dependsOnAssignmentId)
                .orElseThrow(() -> new RuntimeException("Dependency assignment not found: " + dependsOnAssignmentId));

        if (assignment.getId().equals(dependency.getId())) {
            throw new RuntimeException("Assignment cannot depend on itself");
        }

        if (assignmentDependencyRepository.existsByAssignmentIdAndDependsOnAssignmentId(assignmentId, dependsOnAssignmentId)) {
            throw new RuntimeException("Dependency already exists");
        }

        if (createsCircularDependency(assignmentId, dependsOnAssignmentId)) {
            throw new RuntimeException("Circular dependency detected");
        }

        AssignmentDependencyEntity saved = assignmentDependencyRepository.save(
                AssignmentDependencyEntity.builder()
                        .assignmentId(assignmentId)
                        .dependsOnAssignmentId(dependsOnAssignmentId)
                        .build()
        );

        return AssignmentDependencyResponse.builder()
                .assignmentId(saved.getAssignmentId())
                .dependsOnAssignmentId(saved.getDependsOnAssignmentId())
                .dependsOnStatus(dependency.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AssignmentDependencyResponse> getDependencies(Long assignmentId) {
        assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        List<AssignmentDependencyEntity> dependencies = assignmentDependencyRepository
                .findByAssignmentIdOrderByCreatedAtAsc(assignmentId);

        Map<Long, Assignment> dependencyAssignments = assignmentRepository
                .findAllById(dependencies.stream().map(AssignmentDependencyEntity::getDependsOnAssignmentId).toList())
                .stream()
                .collect(HashMap::new, (m, a) -> m.put(a.getId(), a), HashMap::putAll);

        return dependencies.stream()
                .map(dep -> {
                    Assignment dependsOn = dependencyAssignments.get(dep.getDependsOnAssignmentId());
                    return AssignmentDependencyResponse.builder()
                            .assignmentId(dep.getAssignmentId())
                            .dependsOnAssignmentId(dep.getDependsOnAssignmentId())
                            .dependsOnStatus(dependsOn == null ? null : dependsOn.getStatus())
                            .build();
                })
                .toList();
    }

    @Transactional
    public void removeDependency(Long assignmentId, Long dependsOnAssignmentId) {
        long removed = assignmentDependencyRepository.deleteByAssignmentIdAndDependsOnAssignmentId(assignmentId, dependsOnAssignmentId);
        if (removed == 0) {
            throw new RuntimeException("Dependency not found");
        }
    }

    @Transactional(readOnly = true)
    public void validateCanComplete(Long assignmentId) {
        List<AssignmentDependencyEntity> dependencies = assignmentDependencyRepository.findByAssignmentId(assignmentId);
        if (dependencies.isEmpty()) {
            return;
        }

        List<Long> dependencyIds = dependencies.stream()
                .map(AssignmentDependencyEntity::getDependsOnAssignmentId)
                .toList();

        List<Long> incompleteDependencyIds = assignmentRepository.findAllById(dependencyIds)
                .stream()
                .filter(a -> a.getStatus() != AssignmentStatus.COMPLETED)
                .map(Assignment::getId)
                .sorted()
                .toList();

        if (!incompleteDependencyIds.isEmpty()) {
            throw new RuntimeException("Cannot complete assignment. Incomplete dependencies: " + incompleteDependencyIds);
        }
    }

    private boolean createsCircularDependency(Long assignmentId, Long dependsOnAssignmentId) {
        return hasPath(dependsOnAssignmentId, assignmentId, new HashSet<>());
    }

    private boolean hasPath(Long currentAssignmentId, Long targetAssignmentId, Set<Long> visited) {
        if (currentAssignmentId.equals(targetAssignmentId)) {
            return true;
        }
        if (!visited.add(currentAssignmentId)) {
            return false;
        }

        List<AssignmentDependencyEntity> outgoingDependencies = assignmentDependencyRepository.findByAssignmentId(currentAssignmentId);
        for (AssignmentDependencyEntity dependency : outgoingDependencies) {
            if (hasPath(dependency.getDependsOnAssignmentId(), targetAssignmentId, visited)) {
                return true;
            }
        }
        return false;
    }
}
