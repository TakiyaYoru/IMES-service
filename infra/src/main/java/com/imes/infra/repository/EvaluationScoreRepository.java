package com.imes.infra.repository;

import com.imes.infra.entity.EvaluationScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationScoreRepository extends JpaRepository<EvaluationScoreEntity, Long> {

    List<EvaluationScoreEntity> findByEvaluationId(Long evaluationId);

    List<EvaluationScoreEntity> findByEvaluationIdIn(List<Long> evaluationIds);

    Optional<EvaluationScoreEntity> findByEvaluationIdAndCriteriaId(Long evaluationId, Long criteriaId);
}
