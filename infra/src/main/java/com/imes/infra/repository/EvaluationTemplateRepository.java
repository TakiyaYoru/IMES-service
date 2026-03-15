package com.imes.infra.repository;

import com.imes.infra.entity.EvaluationTemplateEntity;
import com.imes.infra.entity.EvaluationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationTemplateRepository extends JpaRepository<EvaluationTemplateEntity, Long> {

    List<EvaluationTemplateEntity> findByIsActiveTrueOrderByCreatedAtDesc();

    List<EvaluationTemplateEntity> findByEvaluationTypeAndIsActiveTrueOrderByCreatedAtDesc(EvaluationType evaluationType);
}
