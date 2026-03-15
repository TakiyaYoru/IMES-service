package com.imes.assignment.service;

import com.imes.assignment.dto.ChecklistItemResponse;
import com.imes.assignment.dto.ChecklistSummaryResponse;
import com.imes.assignment.dto.CreateChecklistItemRequest;
import com.imes.assignment.entity.Assignment;
import com.imes.assignment.entity.AssignmentChecklistEntity;
import com.imes.assignment.repository.AssignmentChecklistRepository;
import com.imes.assignment.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentChecklistService {

    private final AssignmentChecklistRepository assignmentChecklistRepository;
    private final AssignmentRepository assignmentRepository;

    @Transactional
    public ChecklistItemResponse addChecklistItem(Long assignmentId, CreateChecklistItemRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        int nextOrder = assignmentChecklistRepository
                .findTopByAssignmentIdOrderByDisplayOrderDescIdDesc(assignment.getId())
                .map(item -> item.getDisplayOrder() + 1)
                .orElse(1);

        AssignmentChecklistEntity entity = AssignmentChecklistEntity.builder()
                .assignmentId(assignment.getId())
                .itemText(request.itemText().trim())
                .isCompleted(false)
                .displayOrder(nextOrder)
                .build();

        AssignmentChecklistEntity saved = assignmentChecklistRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ChecklistSummaryResponse getChecklist(Long assignmentId) {
        assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        List<ChecklistItemResponse> items = assignmentChecklistRepository
                .findByAssignmentIdOrderByDisplayOrderAscIdAsc(assignmentId)
                .stream()
                .map(this::toResponse)
                .toList();

        return toSummary(assignmentId, items);
    }

    @Transactional
    public ChecklistItemResponse toggleChecklistItem(Long itemId) {
        AssignmentChecklistEntity item = assignmentChecklistRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Checklist item not found: " + itemId));

        item.setIsCompleted(!Boolean.TRUE.equals(item.getIsCompleted()));
        AssignmentChecklistEntity saved = assignmentChecklistRepository.saveAndFlush(item);
        return toResponse(saved);
    }

    @Transactional
    public void deleteChecklistItem(Long itemId) {
        AssignmentChecklistEntity item = assignmentChecklistRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Checklist item not found: " + itemId));

        assignmentChecklistRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public ChecklistSummaryResponse getChecklistSummary(Long assignmentId) {
        assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        List<ChecklistItemResponse> items = assignmentChecklistRepository
                .findByAssignmentIdOrderByDisplayOrderAscIdAsc(assignmentId)
                .stream()
                .map(this::toResponse)
                .toList();

        return toSummary(assignmentId, items);
    }

    private ChecklistSummaryResponse toSummary(Long assignmentId, List<ChecklistItemResponse> items) {
        long totalItems = assignmentChecklistRepository.countByAssignmentId(assignmentId);
        long completedItems = assignmentChecklistRepository.countByAssignmentIdAndIsCompletedTrue(assignmentId);
        double completionPercentage = totalItems == 0
                ? 0.0
                : Math.round((completedItems * 10000.0) / totalItems) / 100.0;

        return ChecklistSummaryResponse.builder()
                .items(items)
                .totalItems(totalItems)
                .completedItems(completedItems)
                .completionPercentage(completionPercentage)
                .build();
    }

    private ChecklistItemResponse toResponse(AssignmentChecklistEntity entity) {
        return ChecklistItemResponse.builder()
                .id(entity.getId())
                .assignmentId(entity.getAssignmentId())
                .itemText(entity.getItemText())
                .isCompleted(Boolean.TRUE.equals(entity.getIsCompleted()))
                .displayOrder(entity.getDisplayOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
