package com.imes.infra.repository;

import com.imes.common.constant.Role;
import com.imes.infra.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    Optional<UserEntity> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Page<UserEntity> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<UserEntity> findByRole(Role role, Pageable pageable);
    
    @Query("SELECT u FROM UserEntity u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive)")
    Page<UserEntity> searchUsers(
        @Param("keyword") String keyword,
        @Param("role") Role role,
        @Param("isActive") Boolean isActive,
        Pageable pageable
    );
    
    List<UserEntity> findByRoleAndIsActive(Role role, Boolean isActive);
}
