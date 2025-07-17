package com.smartfactory.smartmes_insight.domain.production;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductionResultRepository extends JpaRepository<ProductionResult, Long> {

    // 특정 작업 지시의 생산 실적 조회
    List<ProductionResult> findByWorkOrderId(Long workOrderId);

    // 특정 기간의 생산 실적 조회
    List<ProductionResult> findByRecordedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 특정 작업 지시와 기간의 생산 실적 조회
    List<ProductionResult> findByWorkOrderIdAndRecordedAtBetween(Long workOrderId, LocalDateTime startDate, LocalDateTime endDate);

    // 전체 생산 실적 조회 (등록일 역순)
    List<ProductionResult> findAllByOrderByRecordedAtDesc();

    // 특정 작업 지시의 생산 실적 조회 (등록일 역순)
    List<ProductionResult> findByWorkOrderIdOrderByRecordedAtDesc(Long workOrderId);

    // 최근 생산 실적 조회 (개수 제한)
    List<ProductionResult> findTop10ByOrderByRecordedAtDesc();

    // 불량률이 높은 실적 조회 (불량률 기준)
    @Query("SELECT pr FROM ProductionResult pr WHERE " +
           "(CAST(pr.quantityDefective AS double) / CAST(pr.quantityProduced AS double) * 100) >= :defectiveRate " +
           "AND pr.quantityProduced > 0 " +
           "ORDER BY pr.recordedAt DESC")
    List<ProductionResult> findByDefectiveRateGreaterThanEqual(@Param("defectiveRate") double defectiveRate);

    // 특정 설비의 생산 실적 조회
    @Query("SELECT pr FROM ProductionResult pr JOIN pr.workOrder wo WHERE wo.facility.id = :facilityId")
    List<ProductionResult> findByFacilityId(@Param("facilityId") Long facilityId);

    // 특정 설비와 기간의 생산 실적 조회
    @Query("SELECT pr FROM ProductionResult pr JOIN pr.workOrder wo " +
           "WHERE wo.facility.id = :facilityId AND pr.recordedAt BETWEEN :startDate AND :endDate")
    List<ProductionResult> findByFacilityIdAndRecordedAtBetween(
            @Param("facilityId") Long facilityId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 생산량 기준 상위 실적 조회
    List<ProductionResult> findTop10ByOrderByQuantityProducedDesc();
}
