package com.imes.infra.repository;

import com.imes.infra.entity.AnomalyEntity;
import com.imes.infra.entity.AnomalyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnomalyRepository extends JpaRepository<AnomalyEntity, Long> {

    List<AnomalyEntity> findByAnomalyDateBetweenAndIsActiveTrueOrderByAnomalyDateDescCreatedAtDesc(
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsByInternProfileIdAndAnomalyDateAndAnomalyTypeAndIsActiveTrue(
            Long internProfileId,
            LocalDate anomalyDate,
            AnomalyType anomalyType
    );
}
