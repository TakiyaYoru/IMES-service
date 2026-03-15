package com.imes.intern.controller;

import com.imes.common.dto.ApiResponse;
import com.imes.common.dto.PageResponse;
import com.imes.common.dto.request.CreateInternProfileRequest;
import com.imes.common.dto.request.UpdateInternProfileRequest;
import com.imes.common.dto.response.InternProfileResponse;
import com.imes.common.dto.response.InternSummaryResponse;
import com.imes.core.service.InternProfileService;
import com.imes.core.service.InternSummaryService;
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
    private final InternSummaryService internSummaryService;

    public InternProfileController(InternProfileService internProfileService,
                                   InternSummaryService internSummaryService) {
        this.internProfileService = internProfileService;
        this.internSummaryService = internSummaryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InternProfileResponse>>> getAllInterns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long mentorId,
            @RequestParam(required = false) Long departmentId) {
        logger.info("[INTERN-SERVICE] Get all interns - page: {}, size: {}, keyword: {}, status: {}", page, size, keyword, status);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<InternProfileResponse> response;
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
        return ResponseEntity.ok(ApiResponse.success(internProfileService.getInternProfileById(id)));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<InternProfileResponse>> getInternByEmail(@PathVariable String email) {
        logger.info("[INTERN-SERVICE] Get intern by email: {}", email);
        return ResponseEntity.ok(ApiResponse.success(internProfileService.getInternProfileByEmail(email)));
    }

    /**
     * GET /interns/me
     * Trả về intern profile của user đang đăng nhập.
     * Dùng X-User-Id header do gateway inject để lookup profile theo userId.
     * Fallback: nếu không tìm theo userId thì thử theo email (X-Username).
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<InternProfileResponse>> getMyProfile(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Username", required = false) String username) {
        if (username != null && !username.isBlank()) {
            try {
                logger.info("[INTERN-SERVICE] Get my intern profile by email: {}", username);
                return ResponseEntity.ok(ApiResponse.success(internProfileService.getInternProfileByEmail(username)));
            } catch (Exception ignored) {
                // fall through to id lookup for backward compatibility with existing demo data
            }
        }
        if (userId != null) {
            try {
                logger.info("[INTERN-SERVICE] Get my intern profile by userId: {}", userId);
                return ResponseEntity.ok(ApiResponse.success(internProfileService.getInternProfileById(userId)));
            } catch (Exception ignored) {
                // continue to return 400 below
            }
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("0400", "Missing X-User-Id or X-Username header"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InternProfileResponse>> createIntern(@RequestBody CreateInternProfileRequest request) {
        logger.info("[INTERN-SERVICE] Create intern: {}", request.email());
        return ResponseEntity.ok(ApiResponse.success(internProfileService.createInternProfile(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InternProfileResponse>> updateIntern(
            @PathVariable Long id,
            @RequestBody UpdateInternProfileRequest request) {
        logger.info("[INTERN-SERVICE] Update intern: id={}", id);
        return ResponseEntity.ok(ApiResponse.success(internProfileService.updateInternProfile(id, request)));
    }

    /**
     * PATCH /interns/{id}/status
     * Chuyển trạng thái intern theo State Machine với business rule validation.
     * Transitions: NEW-ACTIVE, ACTIVE-ON_LEAVE/COMPLETED/TERMINATED, ON_LEAVE-ACTIVE/TERMINATED
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<InternProfileResponse>> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        logger.info("[INTERN-SERVICE] Change status (state machine): id={}, newStatus={}", id, status);
        return ResponseEntity.ok(ApiResponse.success(internProfileService.changeStatus(id, status)));
    }

    /**
     * GET /interns/{id}/summary
     * Trả về tổng hợp: profile + attendance stats + latest evaluation.
     * Dùng cho HR decision support.
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<ApiResponse<InternSummaryResponse>> getSummary(@PathVariable Long id) {
        logger.info("[INTERN-SERVICE] Get summary for intern: {}", id);
        return ResponseEntity.ok(ApiResponse.success(internSummaryService.getSummary(id)));
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<ApiResponse<PageResponse<InternProfileResponse>>> getInternsByMentor(
            @PathVariable Long mentorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(internProfileService.getInternsByMentor(mentorId, pageable)));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<PageResponse<InternProfileResponse>>> getInternsByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(internProfileService.getInternsByDepartment(departmentId, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIntern(@PathVariable Long id) {
        logger.info("[INTERN-SERVICE] Delete intern: id={}", id);
        internProfileService.deleteInternProfile(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Intern Service is running"));
    }
}
