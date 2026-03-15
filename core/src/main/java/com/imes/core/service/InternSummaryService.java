package com.imes.core.service;

import com.imes.common.dto.response.InternSummaryResponse;
import com.imes.core.exception.ClientSideException;
import com.imes.core.exception.ErrorCode;
import com.imes.infra.entity.AttendanceStatus;
import com.imes.infra.entity.EvaluationEntity;
import com.imes.infra.entity.InternProfileEntity;
import com.imes.infra.repository.AttendanceRepository;
import com.imes.infra.repository.EvaluationRepository;
import com.imes.infra.repository.InternProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternSummaryService {

    private final InternProfileRepository internProfileRepository;
    private final AttendanceRepository attendanceRepository;
    private final EvaluationRepository evaluationRepository;

    public InternSummaryResponse getSummary(Long internId) {
        InternProfileEntity intern = internProfileRepository.findByIdAndIsActiveTrue(internId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy hồ sơ intern"));

        // Attendance stats: from startDate (or 90 days ago) to today
        LocalDate from = intern.getStartDate() != null ? intern.getStartDate() : LocalDate.now().minusDays(90);
        LocalDate to = intern.getEndDate() != null ? intern.getEndDate() : LocalDate.now();

        long total    = attendanceRepository.countTotalDays(internId, from, to);
        long present  = attendanceRepository.countByInternAndStatusInRange(internId, AttendanceStatus.PRESENT, from, to);
        long late     = attendanceRepository.countByInternAndStatusInRange(internId, AttendanceStatus.LATE, from, to);
        long absent   = attendanceRepository.countByInternAndStatusInRange(internId, AttendanceStatus.ABSENT, from, to);
        long leave    = attendanceRepository.countByInternAndStatusInRange(internId, AttendanceStatus.LEAVE, from, to);
        Double hoursRaw = attendanceRepository.calculateTotalHours(internId, from, to);
        double hours  = hoursRaw != null ? hoursRaw : 0.0;
        double rate   = total > 0 ? (double)(present + late) / total * 100.0 : 0.0;

        // Latest evaluation
        List<EvaluationEntity> evals = evaluationRepository.findByInternProfileIdOrderByCreatedAtDesc(internId);
        EvaluationEntity latest = evals.isEmpty() ? null :
                evals.stream().max(Comparator.comparing(e -> e.getCreatedAt())).orElse(null);

        return new InternSummaryResponse(
                intern.getId(),
                intern.getFullName(),
                intern.getEmail(),
                intern.getStudentId(),
                intern.getMajor(),
                intern.getUniversity(),
                intern.getStatus().name(),
                intern.getStartDate(),
                intern.getEndDate(),
                intern.getMentorId(),
                intern.getDepartmentId(),
                total,
                present,
                late,
                absent,
                leave,
                Math.round(rate * 10.0) / 10.0,
                hours,
                latest != null ? latest.getId() : null,
                latest != null ? latest.getEvaluationType().name() : null,
                latest != null ? latest.getTotalScore() : null,
                latest != null ? latest.getStatus().name() : null
        );
    }
}
