package com.imes.infra.repository;

import com.imes.infra.entity.EvaluationEntity;
import com.imes.infra.entity.EvaluationStatus;
import com.imes.infra.entity.EvaluationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<EvaluationEntity, Long> {

    List<EvaluationEntity> findByInternProfileIdOrderByCreatedAtDesc(Long internProfileId);

    List<EvaluationEntity> findByEvaluatorIdOrderByCreatedAtDesc(Long evaluatorId);

    List<EvaluationEntity> findByStatusOrderByCreatedAtDesc(EvaluationStatus status);

    List<EvaluationEntity> findByEvaluationTypeOrderByCreatedAtDesc(EvaluationType evaluationType);

    boolean existsByInternProfileIdAndEvaluationTypeAndPeriodStartAndPeriodEnd(
            Long internProfileId,
            EvaluationType evaluationType,
            LocalDate periodStart,
            LocalDate periodEnd
    );
}
