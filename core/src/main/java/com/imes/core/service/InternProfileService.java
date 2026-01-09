package com.imes.core.service;

import com.imes.common.dto.request.CreateInternProfileRequest;
import com.imes.common.dto.request.UpdateInternProfileRequest;
import com.imes.common.dto.response.InternProfileResponse;
import com.imes.common.dto.PageResponse;
import com.imes.core.exception.ClientSideException;
import com.imes.core.exception.ErrorCode;
import com.imes.infra.entity.InternProfileEntity;
import com.imes.infra.entity.InternStatus;
import com.imes.infra.repository.InternProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class InternProfileService {

    private final InternProfileRepository internProfileRepository;

    public InternProfileResponse createInternProfile(CreateInternProfileRequest request) {
        if (internProfileRepository.existsByEmailAndIsActiveTrue(request.email())) {
            throw new ClientSideException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email đã tồn tại");
        }

        InternProfileEntity entity = InternProfileEntity.builder()
                .email(request.email())
                .fullName(request.fullName())
                .phoneNumber(request.phoneNumber())
                .major(request.major())
                .university(request.university())
                .gpa(request.gpa())
                .skills(request.skills())
                .status(InternStatus.NEW)
                .isActive(true)
                .build();

        entity = internProfileRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public InternProfileResponse getInternProfileById(Long id) {
        InternProfileEntity entity = internProfileRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy hồ sơ"));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public InternProfileResponse getInternProfileByEmail(String email) {
        InternProfileEntity entity = internProfileRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ClientSideException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy hồ sơ"));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public PageResponse<InternProfileResponse> getAllInternProfiles(Pageable pageable) {
        Page<InternProfileEntity> page = internProfileRepository.findAllActive(pageable);
        return convertToPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<InternProfileResponse> searchInternProfiles(String keyword, Pageable pageable) {
        Page<InternProfileEntity> page = internProfileRepository.searchByKeyword(keyword, pageable);
        return convertToPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<InternProfileResponse> getInternProfilesByStatus(String status, Pageable pageable) {
        try {
            InternStatus internStatus = InternStatus.valueOf(status.toUpperCase());
            Page<InternProfileEntity> page = internProfileRepository.findByStatus(internStatus, pageable);
            return convertToPageResponse(page);
        } catch (IllegalArgumentException e) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Trạng thái không hợp lệ");
        }
    }

    public InternProfileResponse updateInternProfile(Long id, UpdateInternProfileRequest request) {
        InternProfileEntity entity = internProfileRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy hồ sơ"));

        if (request.email() != null && !request.email().equals(entity.getEmail())) {
            if (internProfileRepository.existsByEmailAndIsActiveTrue(request.email())) {
                throw new ClientSideException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email đã tồn tại");
            }
            entity.setEmail(request.email());
        }

        if (request.fullName() != null) entity.setFullName(request.fullName());
        if (request.phoneNumber() != null) entity.setPhoneNumber(request.phoneNumber());
        if (request.major() != null) entity.setMajor(request.major());
        if (request.university() != null) entity.setUniversity(request.university());
        if (request.gpa() != null) entity.setGpa(request.gpa());
        if (request.skills() != null) entity.setSkills(request.skills());
        if (request.status() != null) {
            try {
                entity.setStatus(InternStatus.valueOf(request.status().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Trạng thái không hợp lệ");
            }
        }

        entity = internProfileRepository.save(entity);
        return toResponse(entity);
    }

    public void deleteInternProfile(Long id) {
        InternProfileEntity entity = internProfileRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy hồ sơ"));
        entity.setIsActive(false);
        internProfileRepository.save(entity);
    }

    public InternProfileResponse updateStartDate(Long id, LocalDate startDate) {
        InternProfileEntity entity = internProfileRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy hồ sơ"));

        if (entity.getEndDate() != null && startDate.isAfter(entity.getEndDate())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Ngày bắt đầu phải trước ngày kết thúc");
        }

        entity.setStartDate(startDate);
        entity = internProfileRepository.save(entity);
        return toResponse(entity);
    }

    public InternProfileResponse updateEndDate(Long id, LocalDate endDate) {
        InternProfileEntity entity = internProfileRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy hồ sơ"));

        if (entity.getStartDate() != null && endDate.isBefore(entity.getStartDate())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Ngày kết thúc phải sau ngày bắt đầu");
        }

        entity.setEndDate(endDate);
        entity = internProfileRepository.save(entity);
        return toResponse(entity);
    }

    public InternProfileResponse updateStatus(Long id, String status) {
        InternProfileEntity entity = internProfileRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy hồ sơ"));

        try {
            entity.setStatus(InternStatus.valueOf(status.toUpperCase()));
            entity = internProfileRepository.save(entity);
            return toResponse(entity);
        } catch (IllegalArgumentException e) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Trạng thái không hợp lệ");
        }
    }

    @Transactional(readOnly = true)
    public long countTotalInternProfiles() {
        return internProfileRepository.countByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public long countInternProfilesByStatus(InternStatus status) {
        return internProfileRepository.countByStatusAndIsActiveTrue(status);
    }

    private InternProfileResponse toResponse(InternProfileEntity entity) {
        return new InternProfileResponse(
                entity.getId(),
                entity.getEmail(),
                entity.getFullName(),
                entity.getPhoneNumber(),
                entity.getMajor(),
                entity.getUniversity(),
                entity.getGpa(),
                entity.getSkills(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStatus().name(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private PageResponse<InternProfileResponse> convertToPageResponse(Page<InternProfileEntity> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}
