package com.imes.core.service;

import com.imes.common.dto.response.*;
import com.imes.infra.entity.AttendanceEntity;
import com.imes.infra.entity.AttendanceStatus;
import com.imes.infra.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceAnalyticsService {

    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    public AttendanceMonthlySummaryResponse getMonthlySummary(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        List<AttendanceEntity> records = attendanceRepository.findAllInDateRange(startDate, endDate);

        int present = countByStatus(records, AttendanceStatus.PRESENT);
        int late = countByStatus(records, AttendanceStatus.LATE);
        int absent = countByStatus(records, AttendanceStatus.ABSENT);
        int leave = countByStatus(records, AttendanceStatus.LEAVE);
        int halfDay = countByStatus(records, AttendanceStatus.HALF_DAY);

        double attendanceRate = percentage(present + late, records.size());

        return new AttendanceMonthlySummaryResponse(
                year,
                month,
                records.size(),
                present,
                late,
                absent,
                leave,
                halfDay,
                attendanceRate
        );
    }

    @Transactional(readOnly = true)
    public AttendanceTrendsResponse getTrends(LocalDate startDate, LocalDate endDate, String granularity) {
        String normalized = normalizeGranularity(granularity);
        List<AttendanceEntity> records = attendanceRepository.findAllInDateRange(startDate, endDate);

        Map<String, List<AttendanceEntity>> grouped = records.stream()
                .collect(Collectors.groupingBy(a -> toTrendLabel(a.getDate(), normalized), TreeMap::new, Collectors.toList()));

        List<AttendanceTrendPointResponse> points = grouped.entrySet().stream()
                .map(entry -> {
                    List<AttendanceEntity> bucket = entry.getValue();
                    return new AttendanceTrendPointResponse(
                            entry.getKey(),
                            bucket.size(),
                            countByStatus(bucket, AttendanceStatus.PRESENT),
                            countByStatus(bucket, AttendanceStatus.LATE),
                            countByStatus(bucket, AttendanceStatus.ABSENT),
                            countByStatus(bucket, AttendanceStatus.LEAVE),
                            countByStatus(bucket, AttendanceStatus.HALF_DAY)
                    );
                })
                .toList();

        return new AttendanceTrendsResponse(normalized, points);
    }

    @Transactional(readOnly = true)
    public List<TopPerformerResponse> getTopPerformers(LocalDate startDate, LocalDate endDate, int limit) {
        List<AttendanceEntity> records = attendanceRepository.findAllInDateRange(startDate, endDate);
        Map<Long, List<AttendanceEntity>> groupedByIntern = records.stream()
            .collect(Collectors.groupingBy(AttendanceEntity::getInternProfileId));

        return groupedByIntern.entrySet().stream()
            .map(entry -> {
                Long internId = entry.getKey();
                List<AttendanceEntity> internRecords = entry.getValue();
                    int present = countByStatus(internRecords, AttendanceStatus.PRESENT);
                    int late = countByStatus(internRecords, AttendanceStatus.LATE);
                    int absent = countByStatus(internRecords, AttendanceStatus.ABSENT);
                    double attendanceRate = percentage(present + late, internRecords.size());
                    BigDecimal totalHours = internRecords.stream()
                            .map(AttendanceEntity::getTotalHours)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new TopPerformerResponse(
                            internId,
                            attendanceRate,
                            present,
                            late,
                            absent,
                            totalHours
                    );
                })
                .sorted(Comparator.comparing(TopPerformerResponse::attendanceRate).reversed()
                        .thenComparing(TopPerformerResponse::presentDays, Comparator.reverseOrder()))
                .limit(Math.max(limit, 1))
                .toList();
    }

    @Transactional(readOnly = true)
    public PunctualityDistributionResponse getPunctualityDistribution(LocalDate startDate, LocalDate endDate) {
        List<AttendanceEntity> records = attendanceRepository.findAllInDateRange(startDate, endDate);
        int onTimeCount = countByStatus(records, AttendanceStatus.PRESENT);
        int lateCount = countByStatus(records, AttendanceStatus.LATE);
        int total = onTimeCount + lateCount;

        return new PunctualityDistributionResponse(
                onTimeCount,
                lateCount,
                percentage(onTimeCount, total),
                percentage(lateCount, total)
        );
    }

    private int countByStatus(List<AttendanceEntity> records, AttendanceStatus status) {
        return (int) records.stream().filter(r -> r.getStatus() == status).count();
    }

    private double percentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return Math.round((numerator * 10000.0) / denominator) / 100.0;
    }

    private String normalizeGranularity(String granularity) {
        if (granularity == null || granularity.isBlank()) {
            return "DAILY";
        }
        String value = granularity.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("DAILY", "WEEKLY", "MONTHLY").contains(value)) {
            throw new IllegalArgumentException("granularity must be DAILY, WEEKLY, or MONTHLY");
        }
        return value;
    }

    private String toTrendLabel(LocalDate date, String granularity) {
        return switch (granularity) {
            case "WEEKLY" -> {
                LocalDate monday = date.with(DayOfWeek.MONDAY);
                int week = date.get(WeekFields.ISO.weekOfWeekBasedYear());
                yield date.getYear() + "-W" + String.format("%02d", week) + " (" + monday + ")";
            }
            case "MONTHLY" -> {
                YearMonth ym = YearMonth.from(date);
                yield ym.toString();
            }
            default -> date.toString();
        };
    }
}
