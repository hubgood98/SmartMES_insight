package com.smartfactory.smartmes_insight.controller;

import com.smartfactory.smartmes_insight.common.ApiResponse;
import com.smartfactory.smartmes_insight.domain.workorder.WorkOrder;
import com.smartfactory.smartmes_insight.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
@Tag(name = "📝 작업 지시 관리", description = "생산 작업 지시 및 진행 상태 관리 API")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @Operation(summary = "작업 지시 생성", description = "새로운 작업 지시를 생성합니다. (ADMIN, MANAGER 권한 필요)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<WorkOrder>> createWorkOrder(
            @RequestParam @Parameter(description = "설비 ID") Long facilityId,
            @RequestParam @Parameter(description = "제품명") String productName,
            @RequestParam @Parameter(description = "생산 수량") Integer quantity) {
        WorkOrder workOrder = workOrderService.createWorkOrder(facilityId, productName, quantity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(workOrder, "작업 지시가 성공적으로 생성되었습니다."));
    }

    @Operation(summary = "전체 작업 지시 조회", description = "모든 작업 지시 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkOrder>>> getAllWorkOrders() {
        List<WorkOrder> workOrders = workOrderService.findAll();
        return ResponseEntity.ok(ApiResponse.success(workOrders, "작업 지시 목록 조회 성공"));
    }

    @Operation(summary = "작업 지시 상세 조회", description = "특정 작업 지시의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkOrder>> getWorkOrderById(
            @PathVariable @Parameter(description = "작업 지시 ID") Long id) {
        Optional<WorkOrder> workOrder = workOrderService.findById(id);
        if (workOrder.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(workOrder.get(), "작업 지시 조회 성공"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, "작업 지시를 찾을 수 없습니다."));
    }

    @Operation(summary = "진행 중인 작업 지시 조회", description = "현재 진행 중이거나 대기 중인 작업 지시 목록을 조회합니다.")
    @GetMapping("/in-progress")
    public ResponseEntity<ApiResponse<List<WorkOrder>>> getInProgressWorkOrders() {
        List<WorkOrder> workOrders = workOrderService.findInProgressWorkOrders();
        return ResponseEntity.ok(ApiResponse.success(workOrders, "진행 중인 작업 지시 조회 성공"));
    }

    @Operation(summary = "상태별 작업 지시 조회", description = "특정 상태의 작업 지시 목록을 조회합니다.")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<WorkOrder>>> getWorkOrdersByStatus(
            @PathVariable @Parameter(description = "작업 상태 (대기중, 진행중, 완료, 취소, 일시정지)") String status) {
        List<WorkOrder> workOrders = workOrderService.findByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(workOrders, "상태별 작업 지시 조회 성공"));
    }

    @Operation(summary = "설비별 작업 지시 조회", description = "특정 설비의 작업 지시 목록을 조회합니다.")
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<ApiResponse<List<WorkOrder>>> getWorkOrdersByFacility(
            @PathVariable @Parameter(description = "설비 ID") Long facilityId) {
        List<WorkOrder> workOrders = workOrderService.findByFacilityId(facilityId);
        return ResponseEntity.ok(ApiResponse.success(workOrders, "설비별 작업 지시 조회 성공"));
    }

    @Operation(summary = "기간별 작업 지시 조회", description = "특정 기간의 작업 지시 목록을 조회합니다.")
    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<WorkOrder>>> getWorkOrdersByPeriod(
            @RequestParam @Parameter(description = "시작 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @Parameter(description = "종료 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<WorkOrder> workOrders = workOrderService.findByPeriod(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(workOrders, "기간별 작업 지시 조회 성공"));
    }

    @Operation(summary = "완료된 작업 지시 조회", description = "완료 상태인 작업 지시 목록을 조회합니다.")
    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<List<WorkOrder>>> getCompletedWorkOrders() {
        List<WorkOrder> workOrders = workOrderService.findCompletedWorkOrders();
        return ResponseEntity.ok(ApiResponse.success(workOrders, "완료된 작업 지시 조회 성공"));
    }

    @Operation(summary = "작업 지시 수정", description = "작업 지시 정보를 수정합니다. (ADMIN, MANAGER 권한 필요)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateWorkOrder(
            @PathVariable @Parameter(description = "작업 지시 ID") Long id,
            @RequestParam(required = false) @Parameter(description = "제품명") String productName,
            @RequestParam(required = false) @Parameter(description = "생산 수량") Integer quantity,
            @RequestParam(required = false) @Parameter(description = "작업 상태") String status) {
        
        WorkOrder updateData = WorkOrder.builder()
                .productName(productName)
                .quantity(quantity)
                .status(status)
                .build();
        
        workOrderService.updateWorkOrder(id, updateData);
        return ResponseEntity.ok(ApiResponse.success(null, "작업 지시가 성공적으로 수정되었습니다."));
    }

    @Operation(summary = "작업 지시 삭제", description = "작업 지시를 삭제합니다. (ADMIN 권한 필요)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteWorkOrder(
            @PathVariable @Parameter(description = "작업 지시 ID") Long id) {
        workOrderService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent("작업 지시가 성공적으로 삭제되었습니다."));
    }

    @Operation(summary = "작업 시작", description = "작업 지시를 시작 상태로 변경합니다. (MANAGER, OPERATOR 권한 필요)")
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('MANAGER') or hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<Void>> startWork(
            @PathVariable @Parameter(description = "작업 지시 ID") Long id) {
        workOrderService.startWork(id);
        return ResponseEntity.ok(ApiResponse.success(null, "작업이 성공적으로 시작되었습니다."));
    }

    @Operation(summary = "작업 완료", description = "작업 지시를 완료 상태로 변경합니다. (MANAGER, OPERATOR 권한 필요)")
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('MANAGER') or hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<Void>> completeWork(
            @PathVariable @Parameter(description = "작업 지시 ID") Long id) {
        workOrderService.completeWork(id);
        return ResponseEntity.ok(ApiResponse.success(null, "작업이 성공적으로 완료되었습니다."));
    }

    @Operation(summary = "작업 일시정지", description = "작업 지시를 일시정지 상태로 변경합니다. (MANAGER, OPERATOR 권한 필요)")
    @PostMapping("/{id}/pause")
    @PreAuthorize("hasRole('MANAGER') or hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<Void>> pauseWork(
            @PathVariable @Parameter(description = "작업 지시 ID") Long id) {
        workOrderService.pauseWork(id);
        return ResponseEntity.ok(ApiResponse.success(null, "작업이 성공적으로 일시정지되었습니다."));
    }

    @Operation(summary = "작업 취소", description = "작업 지시를 취소 상태로 변경합니다. (ADMIN, MANAGER 권한 필요)")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> cancelWork(
            @PathVariable @Parameter(description = "작업 지시 ID") Long id) {
        workOrderService.cancelWork(id);
        return ResponseEntity.ok(ApiResponse.success(null, "작업이 성공적으로 취소되었습니다."));
    }
}
