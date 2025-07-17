package com.smartfactory.smartmes_insight.service;

import com.smartfactory.smartmes_insight.domain.facility.Facility;
import com.smartfactory.smartmes_insight.domain.facility.FacilityRepository;
import com.smartfactory.smartmes_insight.domain.workorder.WorkOrder;
import com.smartfactory.smartmes_insight.domain.workorder.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final FacilityRepository facilityRepository;

    // 작업 지시 생성
    public WorkOrder createWorkOrder(Long facilityId, String productName, Integer quantity) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("설비를 찾을 수 없습니다."));

        WorkOrder workOrder = WorkOrder.builder()
                .facility(facility)
                .productName(productName)
                .quantity(quantity)
                .status("대기중")
                .build();

        return workOrderRepository.save(workOrder);
    }

    // 작업 지시 저장
    public WorkOrder save(WorkOrder workOrder) {
        return workOrderRepository.save(workOrder);
    }

    // 진행 중인 작업 지시 목록 조회
    @Transactional(readOnly = true)
    public List<WorkOrder> findInProgressWorkOrders() {
        return workOrderRepository.findByStatusIn(List.of("진행중", "대기중"));
    }

    // 특정 상태의 작업 지시 조회
    @Transactional(readOnly = true)
    public List<WorkOrder> findByStatus(String status) {
        return workOrderRepository.findByStatus(status);
    }

    // 특정 설비의 작업 지시 조회
    @Transactional(readOnly = true)
    public List<WorkOrder> findByFacilityId(Long facilityId) {
        return workOrderRepository.findByFacilityId(facilityId);
    }

    // 전체 작업 지시 조회
    @Transactional(readOnly = true)
    public List<WorkOrder> findAll() {
        return workOrderRepository.findAll();
    }

    // 작업 지시 단건 조회
    @Transactional(readOnly = true)
    public Optional<WorkOrder> findById(Long id) {
        return workOrderRepository.findById(id);
    }

    // 작업 시작
    public void startWork(Long workOrderId) {
        WorkOrder workOrder = getWorkOrderOrThrow(workOrderId);
        workOrder.setStatus("진행중");
        workOrder.setStartTime(LocalDateTime.now());
    }

    // 작업 완료 처리
    public void completeWork(Long workOrderId) {
        WorkOrder workOrder = getWorkOrderOrThrow(workOrderId);
        workOrder.setStatus("완료");
        workOrder.setEndTime(LocalDateTime.now());
    }

    // 작업 일시정지
    public void pauseWork(Long workOrderId) {
        WorkOrder workOrder = getWorkOrderOrThrow(workOrderId);
        workOrder.setStatus("일시정지");
    }

    // 작업 취소
    public void cancelWork(Long workOrderId) {
        WorkOrder workOrder = getWorkOrderOrThrow(workOrderId);
        workOrder.setStatus("취소");
    }

    // 작업 지시 정보 수정
    public void updateWorkOrder(Long id, WorkOrder updatedWorkOrder) {
        WorkOrder workOrder = getWorkOrderOrThrow(id);
        workOrder.setProductName(updatedWorkOrder.getProductName());
        workOrder.setQuantity(updatedWorkOrder.getQuantity());
        workOrder.setStatus(updatedWorkOrder.getStatus());
    }

    // 작업 지시 삭제
    public void deleteById(Long id) {
        workOrderRepository.deleteById(id);
    }

    // 특정 기간의 작업 지시 조회
    @Transactional(readOnly = true)
    public List<WorkOrder> findByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return workOrderRepository.findByStartTimeBetween(startDate, endDate);
    }

    // 완료된 작업 지시 조회
    @Transactional(readOnly = true)
    public List<WorkOrder> findCompletedWorkOrders() {
        return workOrderRepository.findByStatus("완료");
    }

    private WorkOrder getWorkOrderOrThrow(Long workOrderId) {
        return workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("작업 지시를 찾을 수 없습니다."));
    }
}
