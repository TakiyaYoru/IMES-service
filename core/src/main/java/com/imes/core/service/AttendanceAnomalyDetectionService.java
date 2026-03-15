package com.imes.core.service;

import com.imes.common.dto.response.AttendanceAnomalyResponse;
import com.imes.infra.entity.*;
import com.imes.infra.repository.AnomalyRepository;
import com.imes.infra.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceAnomalyDetectionService {

    private static final int FREQUENT_LATE_THRESHOLD = 3;
    private static final BigDecimal EXCESSIVE_HOURS_THRESHOLD = BigDecimal.valueOf(10);

    private final AttendanceRepository attendanceRepository;
    private final AnomalyRepository anomalyRepository;

    @Transactional
    public List<AttendanceAnomalyResponse> detectInRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        List<AttendanceEntity> records = attendanceRepository.findAllInDateRange(startDate, endDate);
        List<AnomalyEntity> detected = new ArrayList<>();

        for (AttendanceEntity record : records) {
            if (isForgotCheckout(record)) {
                saveIfNotExists(detected,
                        record.getInternProfileId(),
                        record.getDate(),
                        AnomalyType.FORGOT_CHECK_OUT,
                        AnomalySeverity.HIGH,
                        "Check-in exists but check-out is missing"
                );
            }

            if (isExcessiveHours(record)) {
                saveIfNotExists(detected,
                        record.getInternProfileId(),
                        record.getDate(),
                        AnomalyType.EXCESSIVE_HOURS,
                        AnomalySeverity.MEDIUM,
                        "Worked more than 10 hours in a day"
                );
            }
        }

        Map<Long, List<AttendanceEntity>> byIntern = records.stream()
                .collect(Collectors.groupingBy(AttendanceEntity::getInternProfileId));

        for (Map.Entry<Long, List<AttendanceEntity>> entry : byIntern.entrySet()) {
            Long internId = entry.getKey();
            List<AttendanceEntity> internRecords = entry.getValue();

            long lateCount = internRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
            long absentCount = internRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();

            LocalDate markerDate = internRecords.stream()
                    .map(AttendanceEntity::getDate)
                    .max(Comparator.naturalOrder())
                    .orElse(endDate);

            if (lateCount >= FREQUENT_LATE_THRESHOLD) {
                saveIfNotExists(detected,
                        internId,
                        markerDate,
                        AnomalyType.FREQUENT_LATE_ARRIVAL,
                        AnomalySeverity.MEDIUM,
                        "Late arrival frequency exceeded threshold (>= 3)"
                );
            }

            if (lateCount >= 2 && absentCount >= 2) {
                saveIfNotExists(detected,
                        internId,
                        markerDate,
                        AnomalyType.IRREGULAR_PATTERN,
                        AnomalySeverity.LOW,
                        "Mixed pattern of repeated late and absent records"
                );
            }
        }

        return detected.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceAnomalyResponse> getAnomalies(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date range");
        }
        return anomalyRepository
                .findByAnomalyDateBetweenAndIsActiveTrueOrderByAnomalyDateDescCreatedAtDesc(startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void detectDaily() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(7);
        LocalDate end = today.minusDays(1);
        List<AttendanceAnomalyResponse> created = detectInRange(start, end);
        log.info("[ANOMALY-DETECT] Created {} anomalies for range {} -> {}", created.size(), start, end);
    }

    private boolean isForgotCheckout(AttendanceEntity record) {
        return record.getCheckInTime() != null
                && record.getCheckOutTime() == null
                && record.getStatus() != AttendanceStatus.LEAVE
                && record.getStatus() != AttendanceStatus.ABSENT;
    }

    private boolean isExcessiveHours(AttendanceEntity record) {
        return record.getTotalHours() != null && record.getTotalHours().compareTo(EXCESSIVE_HOURS_THRESHOLD) > 0;
    }

    private void saveIfNotExists(
            List<AnomalyEntity> collector,
            Long internId,
            LocalDate anomalyDate,
            AnomalyType type,
            AnomalySeverity severity,
            String description
    ) {
        boolean exists = anomalyRepository.existsByInternProfileIdAndAnomalyDateAndAnomalyTypeAndIsActiveTrue(
                internId,
                anomalyDate,
                type
        );
        if (exists) {
            return;
        }

        AnomalyEntity saved = anomalyRepository.save(AnomalyEntity.builder()
                .internProfileId(internId)
                .anomalyDate(anomalyDate)
                .anomalyType(type)
                .severity(severity)
                .description(description)
                .resolved(false)
                .isActive(true)
                .build());

        collector.add(saved);
    }

    private AttendanceAnomalyResponse mapToResponse(AnomalyEntity entity) {
        return new AttendanceAnomalyResponse(
                entity.getId(),
                entity.getInternProfileId(),
                entity.getAnomalyDate(),
                entity.getAnomalyType().name(),
                entity.getSeverity().name(),
                entity.getDescription(),
                entity.getResolved(),
                entity.getCreatedAt()
        );
    }
}
