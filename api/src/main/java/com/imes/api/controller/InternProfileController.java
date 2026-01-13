package com.imes.api.controller;

import com.imes.common.dto.request.CreateInternProfileRequest;
import com.imes.common.dto.request.UpdateInternProfileRequest;
import com.imes.common.dto.response.InternProfileResponse;
import com.imes.common.dto.PageResponse;
import com.imes.common.dto.ResponseApi;
import com.imes.core.service.InternProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/intern-profiles")
@RequiredArgsConstructor
public class InternProfileController {

    private final InternProfileService internProfileService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ResponseApi<InternProfileResponse>> createInternProfile(
            @Valid @RequestBody CreateInternProfileRequest request) {
        InternProfileResponse response = internProfileService.createInternProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR', 'INTERN')")
    public ResponseEntity<ResponseApi<InternProfileResponse>> getInternProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(internProfileService.getInternProfileById(id)));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR', 'INTERN')")
    public ResponseEntity<ResponseApi<InternProfileResponse>> getInternProfileByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ResponseApi.success(internProfileService.getInternProfileByEmail(email)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR')")
    public ResponseEntity<ResponseApi<PageResponse<InternProfileResponse>>> getAllInternProfiles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String order) {
        
        Sort.Direction direction = order.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(ResponseApi.success(internProfileService.getAllInternProfiles(pageable)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR')")
    public ResponseEntity<ResponseApi<PageResponse<InternProfileResponse>>> searchInternProfiles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ResponseApi.success(internProfileService.searchInternProfiles(keyword, pageable)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR')")
    public ResponseEntity<ResponseApi<PageResponse<InternProfileResponse>>> getInternProfilesByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ResponseApi.success(internProfileService.getInternProfilesByStatus(status, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ResponseApi<InternProfileResponse>> updateInternProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInternProfileRequest request) {
        return ResponseEntity.ok(ResponseApi.success(internProfileService.updateInternProfile(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ResponseApi<Void>> deleteInternProfile(@PathVariable Long id) {
        internProfileService.deleteInternProfile(id);
        return ResponseEntity.ok(ResponseApi.success(null));
    }

    @PatchMapping("/{id}/start-date")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ResponseApi<InternProfileResponse>> updateStartDate(
            @PathVariable Long id,
            @RequestParam LocalDate startDate) {
        return ResponseEntity.ok(ResponseApi.success(internProfileService.updateStartDate(id, startDate)));
    }

    @PatchMapping("/{id}/end-date")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ResponseApi<InternProfileResponse>> updateEndDate(
            @PathVariable Long id,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(ResponseApi.success(internProfileService.updateEndDate(id, endDate)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ResponseApi<InternProfileResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ResponseApi.success(internProfileService.updateStatus(id, status)));
    }

    @GetMapping("/statistics/total")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ResponseApi<Long>> countTotalInternProfiles() {
        return ResponseEntity.ok(ResponseApi.success(internProfileService.countTotalInternProfiles()));
    }
}
