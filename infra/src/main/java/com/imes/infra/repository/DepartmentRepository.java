package com.imes.infra.repository;

import com.imes.infra.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    Optional<Department> findByName(String name);
    
    Optional<Department> findByCode(String code);
    
    boolean existsByName(String name);
    
    boolean existsByCode(String code);
    
    // Find active departments
    Page<Department> findByIsActiveTrue(Pageable pageable);
    
    List<Department> findByIsActiveTrue();
    
    Optional<Department> findByIdAndIsActiveTrue(Long id);
    
    // Search with filters
    @Query("SELECT d FROM Department d WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:isActive IS NULL OR d.isActive = :isActive)")
    Page<Department> searchDepartments(
        @Param("keyword") String keyword,
        @Param("isActive") Boolean isActive,
        Pageable pageable
    );
    
    // Count active departments
    long countByIsActiveTrue();
    
    // Find by manager
    List<Department> findByManagerIdAndIsActiveTrue(Long managerId);
}
