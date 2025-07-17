package com.smartfactory.smartmes_insight.domain.workorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    // 특정 상태의 작업 지시 조회
    List<WorkOrder> findByStatus(String status);

    // 여러 상태의 작업 지시 조회 (예: 진행중, 대기중)
    List<WorkOrder> findByStatusIn(List<String> statuses);

    // 특정 설비의 작업 지시 조회
    List<WorkOrder> findByFacilityId(Long facilityId);

    // 특정 기간의 작업 지시 조회
    List<WorkOrder> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 생성일 기준 특정 기간의 작업 지시 조회
    List<WorkOrder> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 설비별 진행 중인 작업 지시 조회
    @Query("SELECT w FROM WorkOrder w WHERE w.facility.id = :facilityId AND w.status IN :statuses")
    List<WorkOrder> findByFacilityIdAndStatusIn(@Param("facilityId") Long facilityId, @Param("statuses") List<String> statuses);

    // 최근 생성된 작업 지시 조회 (최신순)
    List<WorkOrder> findAllByOrderByCreatedDateDesc();

    // 우선순위별 작업 지시 조회
    @Query("SELECT w FROM WorkOrder w WHERE w.status = :status ORDER BY w.createdDate ASC")
    List<WorkOrder> findByStatusOrderByCreatedDateAsc(@Param("status") String status);
}
