package com.imes.assignment.controller;

import com.imes.assignment.dto.AssignmentDependencyResponse;
import com.imes.assignment.dto.CreateDependencyRequest;
import com.imes.assignment.service.AssignmentDependencyService;
import com.imes.common.dto.ResponseApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignmentDependencyController {

    private final AssignmentDependencyService assignmentDependencyService;

    @PostMapping("/{id}/dependencies")
    public ResponseApi<AssignmentDependencyResponse> addDependency(
            @PathVariable("id") Long assignmentId,
            @Valid @RequestBody CreateDependencyRequest request
    ) {
        AssignmentDependencyResponse response = assignmentDependencyService.addDependency(assignmentId, request.dependsOnAssignmentId());
        return ResponseApi.success(response);
    }

    @GetMapping("/{id}/dependencies")
    public ResponseApi<List<AssignmentDependencyResponse>> getDependencies(@PathVariable("id") Long assignmentId) {
        List<AssignmentDependencyResponse> response = assignmentDependencyService.getDependencies(assignmentId);
        return ResponseApi.success(response);
    }

    @DeleteMapping("/{id}/dependencies/{dependsOnId}")
    public ResponseApi<String> removeDependency(
            @PathVariable("id") Long assignmentId,
            @PathVariable Long dependsOnId
    ) {
        assignmentDependencyService.removeDependency(assignmentId, dependsOnId);
        return ResponseApi.success("Dependency removed successfully");
    }
}
