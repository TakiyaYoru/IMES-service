package com.imes.core.service;

import com.imes.common.dto.request.EvaluationScoreInputRequest;
import com.imes.common.dto.request.UpdateEvaluationRequest;
import com.imes.common.dto.response.EvaluationResponse;
import com.imes.core.exception.ClientSideException;
import com.imes.infra.entity.*;
import com.imes.infra.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EvaluationRepository evaluationRepository;
    @Mock
    private EvaluationTemplateRepository evaluationTemplateRepository;
    @Mock
    private EvaluationCriteriaRepository evaluationCriteriaRepository;
    @Mock
    private EvaluationScoreRepository evaluationScoreRepository;
    @Mock
    private InternProfileRepository internProfileRepository;

    @InjectMocks
    private EvaluationService evaluationService;

    private EvaluationEntity draftEvaluation;

    @BeforeEach
    void setUp() {
        draftEvaluation = EvaluationEntity.builder()
                .id(1L)
                .internProfileId(4L)
                .templateId(10L)
                .evaluatorId(2L)
                .evaluationType(EvaluationType.MENTOR)
                .periodStart(LocalDate.of(2026, 3, 1))
                .periodEnd(LocalDate.of(2026, 3, 31))
                .status(EvaluationStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void update_ShouldCalculateWeightedScoreAndGrade() {
        EvaluationCriteriaEntity c1 = EvaluationCriteriaEntity.builder()
                .id(100L)
                .templateId(10L)
                .weight(BigDecimal.valueOf(60))
                .maxScore(10)
                .build();
        EvaluationCriteriaEntity c2 = EvaluationCriteriaEntity.builder()
                .id(101L)
                .templateId(10L)
                .weight(BigDecimal.valueOf(40))
                .maxScore(10)
                .build();

        List<EvaluationScoreEntity> storedScores = new ArrayList<>();

        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(draftEvaluation));
        when(evaluationRepository.save(any(EvaluationEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(evaluationCriteriaRepository.findByTemplateIdOrderByDisplayOrderAsc(10L)).thenReturn(List.of(c1, c2));
        when(evaluationScoreRepository.findByEvaluationIdAndCriteriaId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(evaluationScoreRepository.save(any(EvaluationScoreEntity.class))).thenAnswer(inv -> {
            EvaluationScoreEntity e = inv.getArgument(0);
            storedScores.removeIf(s -> s.getCriteriaId().equals(e.getCriteriaId()));
            storedScores.add(e);
            return e;
        });
        when(evaluationScoreRepository.findByEvaluationId(1L)).thenAnswer(inv -> storedScores);

        UpdateEvaluationRequest request = new UpdateEvaluationRequest(
                "Updated",
                List.of(
                        new EvaluationScoreInputRequest(100L, BigDecimal.valueOf(8), "Good", null),
                        new EvaluationScoreInputRequest(101L, BigDecimal.valueOf(9), "Very good", null)
                )
        );

        EvaluationResponse response = evaluationService.update(1L, request);

        assertEquals(new BigDecimal("84.00"), response.totalScore());
        assertEquals("B", response.grade());
    }

    @Test
    void workflow_ShouldTransitionDraftToFinalized() {
        draftEvaluation.setTotalScore(BigDecimal.valueOf(90));
        draftEvaluation.setGrade("A");

        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(draftEvaluation));
        when(evaluationRepository.save(any(EvaluationEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(evaluationScoreRepository.findByEvaluationId(1L)).thenReturn(List.of());

        EvaluationResponse submitted = evaluationService.submit(1L);
        assertEquals("SUBMITTED", submitted.status());

        EvaluationResponse reviewed = evaluationService.review(1L);
        assertEquals("REVIEWED", reviewed.status());

        EvaluationResponse finalized = evaluationService.finalizeEvaluation(1L);
        assertEquals("FINALIZED", finalized.status());
    }

    @Test
    void review_ShouldFailWhenNotSubmitted() {
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(draftEvaluation));

        assertThrows(ClientSideException.class, () -> evaluationService.review(1L));
    }

        @Test
        void create_ShouldSucceed_WhenInputValid() {
        EvaluationTemplateEntity template = EvaluationTemplateEntity.builder()
            .id(10L)
            .name("Mentor Template")
            .evaluationType(EvaluationType.MENTOR)
            .isActive(true)
            .build();

        when(internProfileRepository.findByIdAndIsActiveTrue(4L))
            .thenReturn(Optional.of(mock(InternProfileEntity.class)));
        when(evaluationTemplateRepository.findById(10L)).thenReturn(Optional.of(template));
        when(evaluationRepository.existsByInternProfileIdAndEvaluationTypeAndPeriodStartAndPeriodEnd(
            4L, EvaluationType.MENTOR, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)
        )).thenReturn(false);
        when(evaluationRepository.save(any(EvaluationEntity.class))).thenAnswer(inv -> {
            EvaluationEntity e = inv.getArgument(0);
            e.setId(9L);
            return e;
        });
        when(evaluationScoreRepository.findByEvaluationId(9L)).thenReturn(List.of());

        EvaluationResponse response = evaluationService.create(
            new com.imes.common.dto.request.CreateEvaluationRequest(
                4L,
                10L,
                "mentor",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                "Initial"
            ),
            2L
        );

        assertEquals(9L, response.id());
        assertEquals("DRAFT", response.status());
        assertEquals("MENTOR", response.evaluationType());
        }

        @Test
        void create_ShouldFail_WhenDuplicateEvaluationExists() {
        EvaluationTemplateEntity template = EvaluationTemplateEntity.builder()
            .id(10L)
            .name("Mentor Template")
            .evaluationType(EvaluationType.MENTOR)
            .isActive(true)
            .build();

        when(internProfileRepository.findByIdAndIsActiveTrue(4L))
            .thenReturn(Optional.of(mock(InternProfileEntity.class)));
        when(evaluationTemplateRepository.findById(10L)).thenReturn(Optional.of(template));
        when(evaluationRepository.existsByInternProfileIdAndEvaluationTypeAndPeriodStartAndPeriodEnd(
            4L, EvaluationType.MENTOR, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)
        )).thenReturn(true);

        assertThrows(ClientSideException.class, () -> evaluationService.create(
            new com.imes.common.dto.request.CreateEvaluationRequest(
                4L,
                10L,
                "MENTOR",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                "Duplicate"
            ),
            2L
        ));
        }

        @Test
        void update_ShouldFail_WhenScoreGreaterThanMax() {
        EvaluationCriteriaEntity c1 = EvaluationCriteriaEntity.builder()
            .id(100L)
            .templateId(10L)
            .weight(BigDecimal.valueOf(100))
            .maxScore(10)
            .build();

        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(draftEvaluation));
        when(evaluationCriteriaRepository.findByTemplateIdOrderByDisplayOrderAsc(10L)).thenReturn(List.of(c1));

        UpdateEvaluationRequest request = new UpdateEvaluationRequest(
            "Invalid score",
            List.of(new EvaluationScoreInputRequest(100L, BigDecimal.valueOf(11), null, null))
        );

        assertThrows(ClientSideException.class, () -> evaluationService.update(1L, request));
        }

        @Test
        void submit_ShouldFail_WhenScoreNotCalculated() {
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(draftEvaluation));

        assertThrows(ClientSideException.class, () -> evaluationService.submit(1L));
        }

            @Test
            void getAll_ShouldFilterByStatus() {
            EvaluationEntity submitted = EvaluationEntity.builder()
                .id(3L)
                .status(EvaluationStatus.SUBMITTED)
                .evaluationType(EvaluationType.MENTOR)
                .createdAt(LocalDateTime.now())
                .build();

            when(evaluationRepository.findByStatusOrderByCreatedAtDesc(EvaluationStatus.SUBMITTED))
                .thenReturn(List.of(submitted));
            when(evaluationScoreRepository.findByEvaluationId(3L)).thenReturn(List.of());

            List<EvaluationResponse> response = evaluationService.getAll(null, null, "submitted", null);

            assertEquals(1, response.size());
            assertEquals("SUBMITTED", response.get(0).status());
            }

            @Test
            void getComparison_ShouldAggregateFinalizedEvaluations() {
            EvaluationEntity e1 = EvaluationEntity.builder()
                .id(10L)
                .internProfileId(4L)
                .templateId(2L)
                .evaluatorId(2L)
                .evaluationType(EvaluationType.MENTOR)
                .status(EvaluationStatus.FINALIZED)
                .totalScore(BigDecimal.valueOf(80))
                .finalizedAt(LocalDateTime.of(2026, 3, 10, 0, 0))
                .createdAt(LocalDateTime.of(2026, 3, 1, 0, 0))
                .build();
            EvaluationEntity e2 = EvaluationEntity.builder()
                .id(11L)
                .internProfileId(4L)
                .templateId(2L)
                .evaluatorId(2L)
                .evaluationType(EvaluationType.MENTOR)
                .status(EvaluationStatus.FINALIZED)
                .totalScore(BigDecimal.valueOf(90))
                .finalizedAt(LocalDateTime.of(2026, 3, 20, 0, 0))
                .createdAt(LocalDateTime.of(2026, 3, 2, 0, 0))
                .build();

            when(evaluationRepository.findAll()).thenReturn(List.of(e1, e2));

            var response = evaluationService.getComparison(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

            assertEquals(new BigDecimal("85.00"), response.overallAverageScore());
            assertEquals(1, response.items().size());
            assertEquals(4L, response.items().get(0).internProfileId());
            assertEquals(0, BigDecimal.valueOf(90).compareTo(response.items().get(0).latestScore()));
            }

            @Test
            void getTrends_ShouldReturnMonthlyPoints() {
            EvaluationEntity e1 = EvaluationEntity.builder()
                .id(20L)
                .internProfileId(4L)
                .status(EvaluationStatus.FINALIZED)
                .evaluationType(EvaluationType.MENTOR)
                .totalScore(BigDecimal.valueOf(70))
                .finalizedAt(LocalDateTime.of(2026, 2, 15, 0, 0))
                .createdAt(LocalDateTime.of(2026, 2, 1, 0, 0))
                .build();
            EvaluationEntity e2 = EvaluationEntity.builder()
                .id(21L)
                .internProfileId(5L)
                .status(EvaluationStatus.FINALIZED)
                .evaluationType(EvaluationType.MENTOR)
                .totalScore(BigDecimal.valueOf(90))
                .finalizedAt(LocalDateTime.of(2026, 2, 20, 0, 0))
                .createdAt(LocalDateTime.of(2026, 2, 2, 0, 0))
                .build();

            when(evaluationRepository.findAll()).thenReturn(List.of(e1, e2));

            var response = evaluationService.getTrends(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), "MONTHLY");

            assertEquals("MONTHLY", response.granularity());
            assertEquals(1, response.points().size());
            assertEquals(new BigDecimal("80.00"), response.points().get(0).averageScore());
            }

            @Test
            void getTrends_ShouldThrow_WhenGranularityInvalid() {
            assertThrows(ClientSideException.class, () ->
                evaluationService.getTrends(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "WEEKLY")
            );
            }
}
