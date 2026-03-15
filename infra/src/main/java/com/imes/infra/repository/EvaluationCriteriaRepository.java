package com.imes.infra.repository;

import com.imes.infra.entity.EvaluationCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteriaEntity, Long> {

    List<EvaluationCriteriaEntity> findByTemplateIdOrderByDisplayOrderAsc(Long templateId);
}
