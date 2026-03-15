package com.imes.user.controller;

import com.imes.common.dto.ApiResponse;
import com.imes.common.dto.department.CreateDepartmentRequest;
import com.imes.common.dto.department.DepartmentResponse;
import com.imes.common.dto.department.UpdateDepartmentRequest;
import com.imes.common.dto.PageResponse;
import com.imes.core.service.DepartmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {
    
    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DepartmentResponse>>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive) {
        logger.info("[USER-SERVICE] Get departments - page: {}, size: {}, keyword: {}, isActive: {}", 
                page, size, keyword, isActive);
        PageResponse<DepartmentResponse> departments = departmentService.getAllDepartments(page, size, keyword, isActive);
        return ResponseEntity.ok(ApiResponse.success(departments));
    }
    
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllActiveDepartments() {
        logger.info("[USER-SERVICE] Get all active departments");
        List<DepartmentResponse> departments = departmentService.getAllActiveDepartments();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable Long id) {
        logger.info("[USER-SERVICE] Get department by id: {}", id);
        DepartmentResponse department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {
        logger.info("[USER-SERVICE] Create department request: {}", request.getName());
        DepartmentResponse department = departmentService.createDepartment(request);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        logger.info("[USER-SERVICE] Update department request: id={}", id);
        DepartmentResponse department = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        logger.info("[USER-SERVICE] Soft delete department request: id={}", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreDepartment(@PathVariable Long id) {
        logger.info("[USER-SERVICE] Restore department request: id={}", id);
        departmentService.restoreDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
