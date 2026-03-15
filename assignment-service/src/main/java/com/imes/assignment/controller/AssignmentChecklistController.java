package com.imes.assignment.controller;

import com.imes.assignment.dto.ChecklistItemResponse;
import com.imes.assignment.dto.ChecklistSummaryResponse;
import com.imes.assignment.dto.CreateChecklistItemRequest;
import com.imes.assignment.service.AssignmentChecklistService;
import com.imes.common.dto.ResponseApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
@Slf4j
public class AssignmentChecklistController {

    private final AssignmentChecklistService assignmentChecklistService;

    @PostMapping("/{id}/checklist-items")
    public ResponseApi<ChecklistItemResponse> addChecklistItem(
            @PathVariable("id") Long assignmentId,
            @Valid @RequestBody CreateChecklistItemRequest request
    ) {
        ChecklistItemResponse response = assignmentChecklistService.addChecklistItem(assignmentId, request);
        return ResponseApi.success(response);
    }

    @GetMapping("/{id}/checklist-items")
    public ResponseApi<ChecklistSummaryResponse> getChecklist(@PathVariable("id") Long assignmentId) {
        ChecklistSummaryResponse response = assignmentChecklistService.getChecklist(assignmentId);
        return ResponseApi.success(response);
    }

    @PutMapping("/checklist-items/{itemId}/toggle")
    public ResponseApi<ChecklistItemResponse> toggleChecklistItem(@PathVariable Long itemId) {
        ChecklistItemResponse response = assignmentChecklistService.toggleChecklistItem(itemId);
        return ResponseApi.success(response);
    }

    @DeleteMapping("/checklist-items/{itemId}")
    public ResponseApi<String> deleteChecklistItem(@PathVariable Long itemId) {
        assignmentChecklistService.deleteChecklistItem(itemId);
        return ResponseApi.success("Checklist item deleted successfully");
    }

    @GetMapping("/{id}/checklist-items/summary")
    public ResponseApi<ChecklistSummaryResponse> getChecklistSummary(@PathVariable("id") Long assignmentId) {
        ChecklistSummaryResponse response = assignmentChecklistService.getChecklistSummary(assignmentId);
        return ResponseApi.success(response);
    }
}
