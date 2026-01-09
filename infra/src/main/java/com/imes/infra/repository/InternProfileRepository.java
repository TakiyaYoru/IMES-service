package com.imes.infra.repository;

import com.imes.infra.entity.InternProfileEntity;
import com.imes.infra.entity.InternStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InternProfileRepository extends JpaRepository<InternProfileEntity, Long> {

    Optional<InternProfileEntity> findByEmailAndIsActiveTrue(String email);

    Optional<InternProfileEntity> findByIdAndIsActiveTrue(Long id);

    @Query("SELECT ip FROM InternProfileEntity ip WHERE ip.isActive = true")
    Page<InternProfileEntity> findAllActive(Pageable pageable);

    @Query("SELECT ip FROM InternProfileEntity ip WHERE ip.isActive = true AND " +
           "(LOWER(ip.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ip.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ip.major) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ip.university) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<InternProfileEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT ip FROM InternProfileEntity ip WHERE ip.isActive = true AND ip.status = :status")
    Page<InternProfileEntity> findByStatus(@Param("status") InternStatus status, Pageable pageable);

    boolean existsByEmailAndIsActiveTrue(String email);

    long countByIsActiveTrue();

    long countByStatusAndIsActiveTrue(InternStatus status);
}
