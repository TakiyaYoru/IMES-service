package com.imes.intern.controller;

import com.imes.common.dto.ApiResponse;
import com.imes.common.dto.PageResponse;
import com.imes.common.dto.request.CreateInternProfileRequest;
import com.imes.common.dto.request.UpdateInternProfileRequest;
import com.imes.common.dto.response.InternProfileResponse;
import com.imes.core.service.InternProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interns")
@CrossOrigin(origins = "*")
public class InternProfileController {
    
    private static final Logger logger = LoggerFactory.getLogger(InternProfileController.class);
    private final InternProfileService internProfileService;

    public InternProfileController(InternProfileService internProfileService) {
        this.internProfileService = internProfileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InternProfileResponse>>> getAllInterns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long mentorId,
            @RequestParam(required = false) Long departmentId) {
        logger.info("[INTERN-SERVICE] Get all interns - page: {}, size: {}, keyword: {}, status: {}, mentorId: {}, departmentId: {}", 
                    page, size, keyword, status, mentorId, departmentId);
        
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<InternProfileResponse> response;
        
        // Use advanced search if any filter is provided
        if (keyword != null || status != null || mentorId != null || departmentId != null) {
            response = internProfileService.searchInternProfiles(keyword, status, mentorId, departmentId, pageable);
        } else {
            response = internProfileService.getAllInternProfiles(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InternProfileResponse>> getInternById(@PathVariable Long id) {
        logger.info("[INTERN-SERVICE] Get intern by id: {}", id);
        InternProfileResponse intern = internProfileService.getInternProfileById(id);
        return ResponseEntity.ok(ApiResponse.success(intern));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<InternProfileResponse>> getInternByEmail(@PathVariable String email) {
        logger.info("[INTERN-SERVICE] Get intern by email: {}", email);
        InternProfileResponse intern = internProfileService.getInternProfileByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(intern));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InternProfileResponse>> createIntern(@RequestBody CreateInternProfileRequest request) {
        logger.info("[INTERN-SERVICE] Create intern request for: {}", request.email());
        InternProfileResponse intern = internProfileService.createInternProfile(request);
        return ResponseEntity.ok(ApiResponse.success(intern));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InternProfileResponse>> updateIntern(
            @PathVariable Long id,
            @RequestBody UpdateInternProfileRequest request) {
        logger.info("[INTERN-SERVICE] Update intern request: id={}", id);
        InternProfileResponse intern = internProfileService.updateInternProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success(intern));
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<ApiResponse<PageResponse<InternProfileResponse>>> getInternsByMentor(
            @PathVariable Long mentorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("[INTERN-SERVICE] Get interns by mentor: {}, page: {}, size: {}", mentorId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<InternProfileResponse> response = internProfileService.getInternsByMentor(mentorId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<PageResponse<InternProfileResponse>>> getInternsByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("[INTERN-SERVICE] Get interns by department: {}, page: {}, size: {}", departmentId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<InternProfileResponse> response = internProfileService.getInternsByDepartment(departmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIntern(@PathVariable Long id) {
        logger.info("[INTERN-SERVICE] Delete intern request: id={}", id);
        internProfileService.deleteInternProfile(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Intern Service is running"));
    }
}
