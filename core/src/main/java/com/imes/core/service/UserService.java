package com.imes.core.service;

import com.imes.common.constant.Role;
import com.imes.common.dto.*;
import com.imes.infra.entity.UserEntity;
import com.imes.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    public PageResponse<UserResponse> getAllUsers(int page, int size, String keyword, Role role, Boolean isActive) {
        log.info("Getting all users - page: {}, size: {}, keyword: {}, role: {}, isActive: {}", 
                 page, size, keyword, role, isActive);

        if (page == -1) {
            return getAllUsersWithoutPagination(keyword, role, isActive);
        } else {
            return getAllUsersWithPagination(page, size, keyword, role, isActive);
        }
    }

    private PageResponse<UserResponse> getAllUsersWithoutPagination(String keyword, Role role, Boolean isActive) {
        Sort sort = Sort.by("id").ascending();
        List<UserEntity> users;
        
        if (keyword != null || role != null || isActive != null) {
            Pageable unpaged = Pageable.unpaged(sort);
            users = userRepository.searchUsers(keyword, role, isActive, unpaged).getContent();
        } else {
            users = userRepository.findAll(sort);
        }

        List<UserResponse> content = users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .content(content)
                .pageNumber(-1)
                .pageSize(content.size())
                .totalElements(content.size())
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(content.isEmpty())
                .build();
    }

    private PageResponse<UserResponse> getAllUsersWithPagination(int page, int size, String keyword, Role role, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<UserEntity> userPage = userRepository.searchUsers(keyword, role, isActive, pageable);

        List<UserResponse> content = userPage.getContent().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .content(content)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .empty(userPage.isEmpty())
                .build();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .isActive(true)
                .build();

        UserEntity savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());
        return mapToUserResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user id: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        UserEntity updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getId());
        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        log.info("Changing password for user id: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", id);
    }

    @Transactional
    public UserResponse deleteUser(Long id) {
        log.info("Deleting (deactivating) user id: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        user.setIsActive(false);
        UserEntity deactivatedUser = userRepository.save(user);
        log.info("User deactivated successfully: {}", id);
        return mapToUserResponse(deactivatedUser);
    }

    private UserResponse mapToUserResponse(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
