package com.imes.core.service;

import com.imes.common.dto.request.CreateEvaluationTemplateRequest;
import com.imes.common.dto.request.UpdateEvaluationTemplateRequest;
import com.imes.common.dto.response.EvaluationCriteriaResponse;
import com.imes.common.dto.response.EvaluationTemplateResponse;
import com.imes.core.exception.ClientSideException;
import com.imes.infra.entity.EvaluationCriteriaEntity;
import com.imes.infra.entity.EvaluationTemplateEntity;
import com.imes.infra.entity.EvaluationType;
import com.imes.infra.repository.EvaluationCriteriaRepository;
import com.imes.infra.repository.EvaluationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationTemplateServiceTest {

    @Mock
    private EvaluationTemplateRepository evaluationTemplateRepository;
    @Mock
    private EvaluationCriteriaRepository evaluationCriteriaRepository;

    @InjectMocks
    private EvaluationTemplateService evaluationTemplateService;

    private EvaluationTemplateEntity activeTemplate;

    @BeforeEach
    void setUp() {
        activeTemplate = EvaluationTemplateEntity.builder()
                .id(10L)
                .name("Mentor Monthly")
                .description("Template for mentor")
                .evaluationType(EvaluationType.MENTOR)
                .isActive(true)
                .createdBy(2L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void create_ShouldTrimNameAndSetActive() {
        CreateEvaluationTemplateRequest request = new CreateEvaluationTemplateRequest(
                "  New Template  ",
                "desc",
                "mentor"
        );

        when(evaluationTemplateRepository.save(any(EvaluationTemplateEntity.class))).thenAnswer(inv -> {
            EvaluationTemplateEntity e = inv.getArgument(0);
            e.setId(11L);
            return e;
        });

        EvaluationTemplateResponse response = evaluationTemplateService.create(request, 99L);

        assertEquals(11L, response.id());
        assertEquals("New Template", response.name());
        assertEquals("MENTOR", response.evaluationType());
        assertTrue(response.isActive());
        assertEquals(99L, response.createdBy());
    }

    @Test
    void getById_ShouldReturnTemplate_WhenActive() {
        when(evaluationTemplateRepository.findById(10L)).thenReturn(Optional.of(activeTemplate));

        EvaluationTemplateResponse response = evaluationTemplateService.getById(10L);

        assertEquals(10L, response.id());
        assertEquals("Mentor Monthly", response.name());
    }

    @Test
    void getById_ShouldThrow_WhenInactive() {
        activeTemplate.setIsActive(false);
        when(evaluationTemplateRepository.findById(10L)).thenReturn(Optional.of(activeTemplate));

        assertThrows(ClientSideException.class, () -> evaluationTemplateService.getById(10L));
    }

    @Test
    void update_ShouldModifyFields() {
        UpdateEvaluationTemplateRequest request = new UpdateEvaluationTemplateRequest(
                " Updated Name ",
                "new desc",
                "final",
                false
        );

        when(evaluationTemplateRepository.findById(10L)).thenReturn(Optional.of(activeTemplate));
        when(evaluationTemplateRepository.save(any(EvaluationTemplateEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        EvaluationTemplateResponse response = evaluationTemplateService.update(10L, request);

        assertEquals("Updated Name", response.name());
        assertEquals("FINAL", response.evaluationType());
        assertFalse(response.isActive());
    }

    @Test
    void getCriteriaByTemplate_ShouldMapCriteria() {
        EvaluationCriteriaEntity c1 = EvaluationCriteriaEntity.builder()
                .id(100L)
                .templateId(10L)
                .category("TECH")
                .criteriaName("Code Quality")
                .criteriaDescription("Clean and maintainable")
                .weight(BigDecimal.valueOf(60))
                .maxScore(10)
                .displayOrder(1)
                .build();

        when(evaluationTemplateRepository.findById(10L)).thenReturn(Optional.of(activeTemplate));
        when(evaluationCriteriaRepository.findByTemplateIdOrderByDisplayOrderAsc(10L)).thenReturn(List.of(c1));

        List<EvaluationCriteriaResponse> response = evaluationTemplateService.getCriteriaByTemplate(10L);

        assertEquals(1, response.size());
        assertEquals("Code Quality", response.get(0).criteriaName());
        assertEquals(10, response.get(0).maxScore());
    }

    @Test
    void getAll_ShouldFilterByType_WhenProvided() {
        when(evaluationTemplateRepository.findByEvaluationTypeAndIsActiveTrueOrderByCreatedAtDesc(EvaluationType.MENTOR))
                .thenReturn(List.of(activeTemplate));

        List<EvaluationTemplateResponse> response = evaluationTemplateService.getAll("mentor");

        assertEquals(1, response.size());
        assertEquals("MENTOR", response.get(0).evaluationType());
    }

    @Test
    void delete_ShouldSoftDeleteTemplate() {
        when(evaluationTemplateRepository.findById(10L)).thenReturn(Optional.of(activeTemplate));
        when(evaluationTemplateRepository.save(any(EvaluationTemplateEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        evaluationTemplateService.delete(10L);

        assertFalse(activeTemplate.getIsActive());
        verify(evaluationTemplateRepository).save(activeTemplate);
    }

    @Test
    void create_ShouldThrow_WhenEvaluationTypeInvalid() {
        CreateEvaluationTemplateRequest request = new CreateEvaluationTemplateRequest(
                "Invalid",
                "desc",
                "UNKNOWN"
        );

        assertThrows(ClientSideException.class, () -> evaluationTemplateService.create(request, 2L));
    }
}
