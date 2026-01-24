package com.imes.infra.repository;

import com.imes.infra.entity.AttendanceEntity;
import com.imes.infra.entity.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {

    // Find by ID and active
    Optional<AttendanceEntity> findByIdAndIsActiveTrue(Long id);

    // Find by intern and date
    @Query("SELECT a FROM AttendanceEntity a WHERE a.internProfileId = :internId " +
           "AND a.date = :date AND a.isActive = true")
    Optional<AttendanceEntity> findByInternAndDate(
            @Param("internId") Long internId,
            @Param("date") LocalDate date);

    // Find by intern and date range
    @Query("SELECT a FROM AttendanceEntity a WHERE a.internProfileId = :internId " +
           "AND a.date BETWEEN :startDate AND :endDate AND a.isActive = true ORDER BY a.date DESC")
    List<AttendanceEntity> findByInternAndDateRange(
            @Param("internId") Long internId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find by intern (paginated)
    @Query("SELECT a FROM AttendanceEntity a WHERE a.internProfileId = :internId " +
           "AND a.isActive = true ORDER BY a.date DESC")
    Page<AttendanceEntity> findByInternProfileId(@Param("internId") Long internId, Pageable pageable);

    // Find by status
    @Query("SELECT a FROM AttendanceEntity a WHERE a.status = :status AND a.isActive = true")
    Page<AttendanceEntity> findByStatus(@Param("status") AttendanceStatus status, Pageable pageable);

    // Count by intern and status in date range
    @Query("SELECT COUNT(a) FROM AttendanceEntity a WHERE a.internProfileId = :internId " +
           "AND a.status = :status AND a.date BETWEEN :startDate AND :endDate AND a.isActive = true")
    long countByInternAndStatusInRange(
            @Param("internId") Long internId,
            @Param("status") AttendanceStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Calculate total hours by intern in date range
    @Query("SELECT COALESCE(SUM(a.totalHours), 0) FROM AttendanceEntity a " +
           "WHERE a.internProfileId = :internId AND a.date BETWEEN :startDate AND :endDate " +
           "AND a.isActive = true")
    Double calculateTotalHours(
            @Param("internId") Long internId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Check if attendance exists for date
    @Query("SELECT COUNT(a) > 0 FROM AttendanceEntity a WHERE a.internProfileId = :internId " +
           "AND a.date = :date AND a.isActive = true")
    boolean existsByInternAndDate(@Param("internId") Long internId, @Param("date") LocalDate date);

    // Get attendance rate (PRESENT + LATE / TOTAL)
    @Query("SELECT COUNT(a) FROM AttendanceEntity a WHERE a.internProfileId = :internId " +
           "AND a.date BETWEEN :startDate AND :endDate AND a.isActive = true")
    long countTotalDays(
            @Param("internId") Long internId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
