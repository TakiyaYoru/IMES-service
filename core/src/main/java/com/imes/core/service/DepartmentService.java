package com.imes.core.service;

import com.imes.common.dto.department.CreateDepartmentRequest;
import com.imes.common.dto.department.DepartmentResponse;
import com.imes.common.dto.department.UpdateDepartmentRequest;
import com.imes.common.dto.PageResponse;
import com.imes.core.exception.ClientSideException;
import com.imes.core.exception.ErrorCode;
import com.imes.infra.entity.Department;
import com.imes.infra.entity.UserEntity;
import com.imes.infra.repository.DepartmentRepository;
import com.imes.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public PageResponse<DepartmentResponse> getAllDepartments(int page, int size, String keyword, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        
        Page<Department> departmentPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            departmentPage = departmentRepository.searchDepartments(keyword.trim(), isActive, pageable);
        } else if (isActive != null) {
            departmentPage = departmentRepository.searchDepartments(null, isActive, pageable);
        } else {
            departmentPage = departmentRepository.findAll(pageable);
        }
        
        List<DepartmentResponse> responses = departmentPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<DepartmentResponse>builder()
                .content(responses)
                .pageNumber(departmentPage.getNumber())
                .pageSize(departmentPage.getSize())
                .totalElements(departmentPage.getTotalElements())
                .totalPages(departmentPage.getTotalPages())
                .last(departmentPage.isLast())
                .build();
    }
    
    public List<DepartmentResponse> getAllActiveDepartments() {
        return departmentRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.DEPARTMENT_NOT_FOUND, 
                    "Department not found with id: " + id));
        return toResponse(department);
    }

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        logger.info("Creating department: {}", request.getName());
        
        // Validate unique name
        if (departmentRepository.existsByName(request.getName())) {
            throw new ClientSideException(ErrorCode.DEPARTMENT_NAME_EXISTS, 
                "Department with name '" + request.getName() + "' already exists");
        }
        
        // Validate unique code
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new ClientSideException(ErrorCode.DEPARTMENT_CODE_EXISTS, 
                "Department with code '" + request.getCode() + "' already exists");
        }
        
        // Validate manager if provided
        if (request.getManagerId() != null) {
            validateManager(request.getManagerId());
        }
        
        Department department = Department.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .managerId(request.getManagerId())
                .isActive(true)
                .build();
        
        Department saved = departmentRepository.save(department);
        logger.info("Department created with id: {}", saved.getId());
        
        return toResponse(saved);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {
        logger.info("Updating department: id={}", id);
        
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.DEPARTMENT_NOT_FOUND, 
                    "Department not found with id: " + id));
        
        // Validate unique name if changed
        if (request.getName() != null && !request.getName().equals(department.getName())) {
            if (departmentRepository.existsByName(request.getName())) {
                throw new ClientSideException(ErrorCode.DEPARTMENT_NAME_EXISTS, 
                    "Department with name '" + request.getName() + "' already exists");
            }
            department.setName(request.getName());
        }
        
        // Validate unique code if changed
        if (request.getCode() != null && !request.getCode().equals(department.getCode())) {
            if (departmentRepository.existsByCode(request.getCode())) {
                throw new ClientSideException(ErrorCode.DEPARTMENT_CODE_EXISTS, 
                    "Department with code '" + request.getCode() + "' already exists");
            }
            department.setCode(request.getCode());
        }
        
        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }
        
        // Validate manager if changed
        if (request.getManagerId() != null && !request.getManagerId().equals(department.getManagerId())) {
            validateManager(request.getManagerId());
            department.setManagerId(request.getManagerId());
        }
        
        Department updated = departmentRepository.save(department);
        return toResponse(updated);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        logger.info("Soft deleting department: id={}", id);
        
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.DEPARTMENT_NOT_FOUND, 
                    "Department not found with id: " + id));
        
        department.setIsActive(false);
        departmentRepository.save(department);
        logger.info("Department soft deleted: id={}", id);
    }
    
    @Transactional
    public void restoreDepartment(Long id) {
        logger.info("Restoring department: id={}", id);
        
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.DEPARTMENT_NOT_FOUND, 
                    "Department not found with id: " + id));
        
        department.setIsActive(true);
        departmentRepository.save(department);
        logger.info("Department restored: id={}", id);
    }

    private void validateManager(Long managerId) {
        UserEntity manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.INVALID_DEPARTMENT_MANAGER, 
                    "User with id " + managerId + " not found"));
        
        // Optionally validate manager has appropriate role (MENTOR or ADMIN)
        // This depends on your UserEntity structure
        logger.info("Validated manager: userId={}", managerId);
    }

    private DepartmentResponse toResponse(Department department) {
        String managerName = null;
        if (department.getManagerId() != null) {
            managerName = userRepository.findById(department.getManagerId())
                    .map(UserEntity::getFullName)
                    .orElse(null);
        }
        
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .description(department.getDescription())
                .managerId(department.getManagerId())
                .managerName(managerName)
                .isActive(department.getIsActive())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}
