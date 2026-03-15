package com.imes.core.service;

import com.imes.common.dto.request.CreateEvaluationRequest;
import com.imes.common.dto.request.EvaluationScoreInputRequest;
import com.imes.common.dto.request.UpdateEvaluationRequest;
import com.imes.common.dto.response.EvaluationResponse;
import com.imes.common.dto.response.EvaluationScoreResponse;
import com.imes.common.dto.response.EvaluationComparisonItemResponse;
import com.imes.common.dto.response.EvaluationComparisonResponse;
import com.imes.common.dto.response.EvaluationTrendPointResponse;
import com.imes.common.dto.response.EvaluationTrendsResponse;
import com.imes.common.dto.response.EvaluationCriteriaBreakdownItemResponse;
import com.imes.common.dto.response.EvaluationCriteriaBreakdownResponse;
import com.imes.core.exception.ClientSideException;
import com.imes.core.exception.ErrorCode;
import com.imes.infra.entity.*;
import com.imes.infra.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final EvaluationTemplateRepository evaluationTemplateRepository;
    private final EvaluationCriteriaRepository evaluationCriteriaRepository;
    private final EvaluationScoreRepository evaluationScoreRepository;
    private final InternProfileRepository internProfileRepository;

    @Transactional
    public EvaluationResponse create(CreateEvaluationRequest request, Long evaluatorId) {
        validatePeriod(request.periodStart(), request.periodEnd());

        if (internProfileRepository.findByIdAndIsActiveTrue(request.internProfileId()).isEmpty()) {
            throw new ClientSideException(ErrorCode.INTERN_PROFILE_NOT_FOUND);
        }

        EvaluationTemplateEntity template = evaluationTemplateRepository.findById(request.templateId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Evaluation template not found"));

        EvaluationType type = parseType(request.evaluationType());
        if (!template.getEvaluationType().equals(type)) {
            throw new ClientSideException(ErrorCode.INVALID_INPUT,
                    "evaluationType does not match template type");
        }

        boolean duplicate = evaluationRepository.existsByInternProfileIdAndEvaluationTypeAndPeriodStartAndPeriodEnd(
                request.internProfileId(), type, request.periodStart(), request.periodEnd()
        );
        if (duplicate) {
            throw new ClientSideException(ErrorCode.VALIDATION_ERROR,
                    "Evaluation already exists for this intern/type/period");
        }

        EvaluationEntity saved = evaluationRepository.save(EvaluationEntity.builder()
                .internProfileId(request.internProfileId())
                .templateId(request.templateId())
                .evaluatorId(evaluatorId)
                .evaluationType(type)
                .periodStart(request.periodStart())
                .periodEnd(request.periodEnd())
                .status(EvaluationStatus.DRAFT)
                .overallComment(request.overallComment())
                .build());

        return mapResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EvaluationResponse> getAll(Long internId, Long evaluatorId, String status, String type) {
        List<EvaluationEntity> data;
        if (internId != null) {
            data = evaluationRepository.findByInternProfileIdOrderByCreatedAtDesc(internId);
        } else if (evaluatorId != null) {
            data = evaluationRepository.findByEvaluatorIdOrderByCreatedAtDesc(evaluatorId);
        } else if (status != null && !status.isBlank()) {
            data = evaluationRepository.findByStatusOrderByCreatedAtDesc(parseStatus(status));
        } else if (type != null && !type.isBlank()) {
            data = evaluationRepository.findByEvaluationTypeOrderByCreatedAtDesc(parseType(type));
        } else {
            data = evaluationRepository.findAll().stream()
                    .sorted(Comparator.comparing(EvaluationEntity::getCreatedAt).reversed())
                    .toList();
        }

        return data.stream().map(this::mapResponse).toList();
    }

    @Transactional(readOnly = true)
    public EvaluationResponse getById(Long id) {
        EvaluationEntity entity = getOrThrow(id);
        return mapResponse(entity);
    }

    @Transactional
    public EvaluationResponse update(Long id, UpdateEvaluationRequest request) {
        EvaluationEntity evaluation = getOrThrow(id);
        ensureStatus(evaluation, EvaluationStatus.DRAFT, "Only DRAFT evaluations can be updated");

        if (request.overallComment() != null) {
            evaluation.setOverallComment(request.overallComment());
        }

        if (request.scores() != null) {
            for (EvaluationScoreInputRequest item : request.scores()) {
                upsertScore(evaluation, item);
            }
        }

        recalculateScoreAndGrade(evaluation);
        EvaluationEntity saved = evaluationRepository.save(evaluation);

        return mapResponse(saved);
    }

    @Transactional
    public EvaluationResponse submit(Long id) {
        EvaluationEntity evaluation = getOrThrow(id);
        ensureStatus(evaluation, EvaluationStatus.DRAFT, "Only DRAFT evaluations can be submitted");

        if (evaluation.getTotalScore() == null || evaluation.getGrade() == null) {
            throw new ClientSideException(ErrorCode.VALIDATION_ERROR,
                    "Evaluation must have calculated score and grade before submit");
        }

        evaluation.setStatus(EvaluationStatus.SUBMITTED);
        evaluation.setSubmittedAt(java.time.LocalDateTime.now());
        return mapResponse(evaluationRepository.save(evaluation));
    }

    @Transactional
    public EvaluationResponse review(Long id) {
        EvaluationEntity evaluation = getOrThrow(id);
        ensureStatus(evaluation, EvaluationStatus.SUBMITTED, "Only SUBMITTED evaluations can be reviewed");

        evaluation.setStatus(EvaluationStatus.REVIEWED);
        evaluation.setReviewedAt(java.time.LocalDateTime.now());
        return mapResponse(evaluationRepository.save(evaluation));
    }

    @Transactional
    public EvaluationResponse finalizeEvaluation(Long id) {
        EvaluationEntity evaluation = getOrThrow(id);
        ensureStatus(evaluation, EvaluationStatus.REVIEWED, "Only REVIEWED evaluations can be finalized");

        evaluation.setStatus(EvaluationStatus.FINALIZED);
        evaluation.setFinalizedAt(java.time.LocalDateTime.now());
        return mapResponse(evaluationRepository.save(evaluation));
    }

        @Transactional(readOnly = true)
        public EvaluationComparisonResponse getComparison(LocalDate startDate, LocalDate endDate) {
        List<EvaluationEntity> data = getFinalizedInRange(startDate, endDate);

        Map<Long, List<EvaluationEntity>> byIntern = data.stream()
            .collect(Collectors.groupingBy(EvaluationEntity::getInternProfileId));

        List<EvaluationComparisonItemResponse> items = byIntern.entrySet().stream()
            .map(entry -> {
                List<EvaluationEntity> internEvals = entry.getValue();
                BigDecimal avg = average(internEvals.stream()
                    .map(EvaluationEntity::getTotalScore)
                    .filter(Objects::nonNull)
                    .toList());
                BigDecimal latest = internEvals.stream()
                    .sorted(Comparator.comparing(this::resolveTimeline).reversed())
                    .map(EvaluationEntity::getTotalScore)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

                return new EvaluationComparisonItemResponse(
                    entry.getKey(),
                    internEvals.size(),
                    avg,
                    latest
                );
            })
            .sorted(Comparator.comparing(EvaluationComparisonItemResponse::averageScore).reversed())
            .toList();

        BigDecimal overallAverage = average(data.stream()
            .map(EvaluationEntity::getTotalScore)
            .filter(Objects::nonNull)
            .toList());

        return new EvaluationComparisonResponse(items, overallAverage);
        }

        @Transactional(readOnly = true)
        public EvaluationTrendsResponse getTrends(LocalDate startDate, LocalDate endDate, String granularity) {
        String normalized = normalizeGranularity(granularity);
        List<EvaluationEntity> data = getFinalizedInRange(startDate, endDate);

        Map<String, List<EvaluationEntity>> grouped = data.stream()
            .collect(Collectors.groupingBy(e -> trendLabel(resolveTimeline(e).toLocalDate(), normalized)));

        List<EvaluationTrendPointResponse> points = grouped.entrySet().stream()
            .map(entry -> {
                BigDecimal avg = average(entry.getValue().stream()
                    .map(EvaluationEntity::getTotalScore)
                    .filter(Objects::nonNull)
                    .toList());
                return new EvaluationTrendPointResponse(entry.getKey(), entry.getValue().size(), avg);
            })
            .sorted(Comparator.comparing(EvaluationTrendPointResponse::label))
            .toList();

        return new EvaluationTrendsResponse(normalized, points);
        }

        @Transactional(readOnly = true)
        public EvaluationCriteriaBreakdownResponse getCriteriaBreakdown(LocalDate startDate, LocalDate endDate) {
        List<EvaluationEntity> data = getFinalizedInRange(startDate, endDate);
        if (data.isEmpty()) {
            return new EvaluationCriteriaBreakdownResponse(List.of());
        }

        List<Long> evaluationIds = data.stream().map(EvaluationEntity::getId).toList();
        List<EvaluationScoreEntity> scores = evaluationScoreRepository.findByEvaluationIdIn(evaluationIds);

        Map<Long, EvaluationCriteriaEntity> criteriaMap = evaluationCriteriaRepository.findAllById(
            scores.stream().map(EvaluationScoreEntity::getCriteriaId).distinct().toList()
        ).stream().collect(Collectors.toMap(EvaluationCriteriaEntity::getId, c -> c));

        Map<Long, List<EvaluationScoreEntity>> grouped = scores.stream()
            .collect(Collectors.groupingBy(EvaluationScoreEntity::getCriteriaId));

        List<EvaluationCriteriaBreakdownItemResponse> items = grouped.entrySet().stream()
            .map(entry -> {
                EvaluationCriteriaEntity criteria = criteriaMap.get(entry.getKey());
                if (criteria == null) {
                return null;
                }
                BigDecimal avg = average(entry.getValue().stream().map(EvaluationScoreEntity::getScore).toList());
                return new EvaluationCriteriaBreakdownItemResponse(
                    criteria.getId(),
                    criteria.getCriteriaName(),
                    criteria.getMaxScore(),
                    entry.getValue().size(),
                    avg
                );
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(EvaluationCriteriaBreakdownItemResponse::averageScore).reversed())
            .toList();

        return new EvaluationCriteriaBreakdownResponse(items);
        }

    private void ensureStatus(EvaluationEntity evaluation, EvaluationStatus expected, String message) {
        if (evaluation.getStatus() != expected) {
            throw new ClientSideException(ErrorCode.VALIDATION_ERROR, message);
        }
    }

    private void recalculateScoreAndGrade(EvaluationEntity evaluation) {
        List<EvaluationCriteriaEntity> criteriaList = evaluationCriteriaRepository
                .findByTemplateIdOrderByDisplayOrderAsc(evaluation.getTemplateId());
        List<EvaluationScoreEntity> scoreList = evaluationScoreRepository.findByEvaluationId(evaluation.getId());

        if (criteriaList.isEmpty() || scoreList.isEmpty()) {
            evaluation.setTotalScore(null);
            evaluation.setGrade(null);
            return;
        }

        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (EvaluationCriteriaEntity criteria : criteriaList) {
            EvaluationScoreEntity score = scoreList.stream()
                    .filter(s -> s.getCriteriaId().equals(criteria.getId()))
                    .findFirst()
                    .orElse(null);

            if (score == null) {
                continue;
            }

            if (criteria.getMaxScore() == null || criteria.getMaxScore() <= 0) {
                continue;
            }

            BigDecimal criteriaWeight = criteria.getWeight() != null ? criteria.getWeight() : BigDecimal.ZERO;
            BigDecimal normalizedPercent = score.getScore()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(criteria.getMaxScore()), 6, RoundingMode.HALF_UP);

            weightedSum = weightedSum.add(normalizedPercent.multiply(criteriaWeight));
            totalWeight = totalWeight.add(criteriaWeight);
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            evaluation.setTotalScore(null);
            evaluation.setGrade(null);
            return;
        }

        BigDecimal totalScore = weightedSum.divide(totalWeight, 2, RoundingMode.HALF_UP);
        evaluation.setTotalScore(totalScore);
        evaluation.setGrade(mapGrade(totalScore));
    }

    private String mapGrade(BigDecimal totalScore) {
        double score = totalScore.doubleValue();
        if (score >= 90.0) {
            return "A";
        }
        if (score >= 80.0) {
            return "B";
        }
        if (score >= 70.0) {
            return "C";
        }
        if (score >= 60.0) {
            return "D";
        }
        return "F";
    }

    private void upsertScore(EvaluationEntity evaluation, EvaluationScoreInputRequest item) {
        List<EvaluationCriteriaEntity> criteriaList = evaluationCriteriaRepository
                .findByTemplateIdOrderByDisplayOrderAsc(evaluation.getTemplateId());

        EvaluationCriteriaEntity criteria = criteriaList.stream()
                .filter(c -> c.getId().equals(item.criteriaId()))
                .findFirst()
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                        "Criteria not found in evaluation template"));

        BigDecimal max = BigDecimal.valueOf(criteria.getMaxScore());
        if (item.score().compareTo(max) > 0) {
            throw new ClientSideException(ErrorCode.INVALID_INPUT,
                    "score must be <= maxScore(" + criteria.getMaxScore() + ")");
        }

        EvaluationScoreEntity score = evaluationScoreRepository
                .findByEvaluationIdAndCriteriaId(evaluation.getId(), item.criteriaId())
                .orElseGet(() -> EvaluationScoreEntity.builder()
                        .evaluationId(evaluation.getId())
                        .criteriaId(item.criteriaId())
                        .build());

        score.setScore(item.score());
        score.setComment(item.comment());
        score.setEvidence(item.evidence());
        evaluationScoreRepository.save(score);
    }

    private EvaluationEntity getOrThrow(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Evaluation not found: " + id));
    }

    private EvaluationResponse mapResponse(EvaluationEntity entity) {
        List<EvaluationScoreResponse> scores = evaluationScoreRepository.findByEvaluationId(entity.getId()).stream()
                .map(s -> new EvaluationScoreResponse(
                        s.getId(),
                        s.getEvaluationId(),
                        s.getCriteriaId(),
                        s.getScore(),
                        s.getComment(),
                        s.getEvidence()
                ))
                .toList();

        return new EvaluationResponse(
                entity.getId(),
                entity.getInternProfileId(),
                entity.getTemplateId(),
                entity.getEvaluatorId(),
                entity.getEvaluationType().name(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                entity.getStatus().name(),
                entity.getTotalScore(),
                entity.getGrade(),
                entity.getOverallComment(),
                entity.getSubmittedAt(),
                entity.getReviewedAt(),
                entity.getFinalizedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                scores
        );
    }

    private EvaluationType parseType(String type) {
        try {
            return EvaluationType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new ClientSideException(ErrorCode.INVALID_INPUT,
                    "evaluationType must be one of: SELF, MENTOR, PEER, FINAL");
        }
    }

    private EvaluationStatus parseStatus(String status) {
        try {
            return EvaluationStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new ClientSideException(ErrorCode.INVALID_INPUT,
                    "status must be one of: DRAFT, SUBMITTED, REVIEWED, FINALIZED");
        }
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            throw new ClientSideException(ErrorCode.INVALID_INPUT,
                    "Invalid period: periodStart must be <= periodEnd");
        }
    }

    private List<EvaluationEntity> getFinalizedInRange(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        return evaluationRepository.findAll().stream()
                .filter(e -> e.getStatus() == EvaluationStatus.FINALIZED)
                .filter(e -> {
                    LocalDate d = resolveTimeline(e).toLocalDate();
                    return (!d.isBefore(startDate) && !d.isAfter(endDate));
                })
                .toList();
    }

    private LocalDateTime resolveTimeline(EvaluationEntity e) {
        if (e.getFinalizedAt() != null) {
            return e.getFinalizedAt();
        }
        if (e.getUpdatedAt() != null) {
            return e.getUpdatedAt();
        }
        return e.getCreatedAt();
    }

    private String normalizeGranularity(String granularity) {
        if (granularity == null || granularity.isBlank()) {
            return "MONTHLY";
        }
        String value = granularity.trim().toUpperCase(Locale.ROOT);
        if (!List.of("DAILY", "MONTHLY").contains(value)) {
            throw new ClientSideException(ErrorCode.INVALID_INPUT, "granularity must be DAILY or MONTHLY");
        }
        return value;
    }

    private String trendLabel(LocalDate date, String granularity) {
        if ("DAILY".equals(granularity)) {
            return date.toString();
        }
        return YearMonth.from(date).toString();
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }
}
