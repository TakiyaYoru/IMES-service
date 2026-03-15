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
import static org.mockito.Mockito.when;

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
}
