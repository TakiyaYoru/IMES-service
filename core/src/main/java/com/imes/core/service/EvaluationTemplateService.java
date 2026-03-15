package com.imes.core.service;

import com.imes.common.dto.request.CreateEvaluationTemplateRequest;
import com.imes.common.dto.request.UpdateEvaluationTemplateRequest;
import com.imes.common.dto.response.EvaluationCriteriaResponse;
import com.imes.common.dto.response.EvaluationTemplateResponse;
import com.imes.core.exception.ClientSideException;
import com.imes.core.exception.ErrorCode;
import com.imes.infra.entity.EvaluationCriteriaEntity;
import com.imes.infra.entity.EvaluationTemplateEntity;
import com.imes.infra.entity.EvaluationType;
import com.imes.infra.repository.EvaluationCriteriaRepository;
import com.imes.infra.repository.EvaluationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EvaluationTemplateService {

    private final EvaluationTemplateRepository evaluationTemplateRepository;
    private final EvaluationCriteriaRepository evaluationCriteriaRepository;

    @Transactional
    public EvaluationTemplateResponse create(CreateEvaluationTemplateRequest request, Long userId) {
        EvaluationTemplateEntity entity = EvaluationTemplateEntity.builder()
                .name(request.name().trim())
                .description(request.description())
                .evaluationType(parseEvaluationType(request.evaluationType()))
                .isActive(true)
                .createdBy(userId)
                .build();

        EvaluationTemplateEntity saved = evaluationTemplateRepository.save(entity);
        return mapTemplate(saved);
    }

    @Transactional(readOnly = true)
    public List<EvaluationTemplateResponse> getAll(String evaluationType) {
        List<EvaluationTemplateEntity> templates;
        if (evaluationType == null || evaluationType.isBlank()) {
            templates = evaluationTemplateRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        } else {
            templates = evaluationTemplateRepository.findByEvaluationTypeAndIsActiveTrueOrderByCreatedAtDesc(
                    parseEvaluationType(evaluationType)
            );
        }
        return templates.stream().map(this::mapTemplate).toList();
    }

    @Transactional(readOnly = true)
    public EvaluationTemplateResponse getById(Long id) {
        return mapTemplate(getTemplateOrThrow(id));
    }

    @Transactional
    public EvaluationTemplateResponse update(Long id, UpdateEvaluationTemplateRequest request) {
        EvaluationTemplateEntity entity = getTemplateOrThrow(id);
        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        entity.setEvaluationType(parseEvaluationType(request.evaluationType()));
        if (request.isActive() != null) {
            entity.setIsActive(request.isActive());
        }

        EvaluationTemplateEntity saved = evaluationTemplateRepository.save(entity);
        return mapTemplate(saved);
    }

    @Transactional
    public void delete(Long id) {
        EvaluationTemplateEntity entity = getTemplateOrThrow(id);
        entity.setIsActive(false);
        evaluationTemplateRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<EvaluationCriteriaResponse> getCriteriaByTemplate(Long templateId) {
        getTemplateOrThrow(templateId);
        List<EvaluationCriteriaEntity> criteria = evaluationCriteriaRepository.findByTemplateIdOrderByDisplayOrderAsc(templateId);
        return criteria.stream().map(this::mapCriteria).toList();
    }

    private EvaluationTemplateEntity getTemplateOrThrow(Long id) {
        EvaluationTemplateEntity entity = evaluationTemplateRepository.findById(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Evaluation template not found: " + id));

        if (!Boolean.TRUE.equals(entity.getIsActive())) {
            throw new ClientSideException(ErrorCode.NOT_FOUND, "Evaluation template is inactive: " + id);
        }
        return entity;
    }

    private EvaluationType parseEvaluationType(String raw) {
        try {
            return EvaluationType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new ClientSideException(ErrorCode.INVALID_INPUT,
                    "evaluationType must be one of: SELF, MENTOR, PEER, FINAL");
        }
    }

    private EvaluationTemplateResponse mapTemplate(EvaluationTemplateEntity entity) {
        return new EvaluationTemplateResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getEvaluationType().name(),
                entity.getIsActive(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private EvaluationCriteriaResponse mapCriteria(EvaluationCriteriaEntity entity) {
        return new EvaluationCriteriaResponse(
                entity.getId(),
                entity.getTemplateId(),
                entity.getCategory(),
                entity.getCriteriaName(),
                entity.getCriteriaDescription(),
                entity.getWeight(),
                entity.getMaxScore(),
                entity.getDisplayOrder()
        );
    }
}
