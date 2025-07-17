package com.smartfactory.smartmes_insight.controller;

import com.smartfactory.smartmes_insight.common.ApiResponse;
import com.smartfactory.smartmes_insight.domain.production.ProductionResult;
import com.smartfactory.smartmes_insight.service.ProductionResultService;
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
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/production-results")
@RequiredArgsConstructor
@Tag(name = "📈 생산 실적 관리", description = "생산 실적 등록 및 분석 API")
public class ProductionResultController {

    private final ProductionResultService productionResultService;

    @Operation(summary = "생산 실적 등록", description = "작업 지시에 대한 생산 실적을 등록합니다. (MANAGER, OPERATOR 권한 필요)")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<ProductionResult>> recordProduction(
            @RequestParam @Parameter(description = "작업 지시 ID") Long workOrderId,
            @RequestParam @Parameter(description = "생산 수량") Integer quantityProduced,
            @RequestParam(required = false, defaultValue = "0") @Parameter(description = "불량 수량") Integer quantityDefective) {
        ProductionResult result = productionResultService.recordProduction(workOrderId, quantityProduced, quantityDefective);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "생산 실적이 성공적으로 등록되었습니다."));
    }

    @Operation(summary = "전체 생산 실적 조회", description = "모든 생산 실적 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductionResult>>> getAllProductionResults() {
        List<ProductionResult> results = productionResultService.findAll();
        return ResponseEntity.ok(ApiResponse.success(results, "생산 실적 목록 조회 성공"));
    }

    @Operation(summary = "생산 실적 상세 조회", description = "특정 생산 실적의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductionResult>> getProductionResultById(
            @PathVariable @Parameter(description = "생산 실적 ID") Long id) {
        Optional<ProductionResult> result = productionResultService.findById(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(result.get(), "생산 실적 조회 성공"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, "생산 실적을 찾을 수 없습니다."));
    }

    @Operation(summary = "작업 지시별 생산 실적 조회", description = "특정 작업 지시의 생산 실적 목록을 조회합니다.")
    @GetMapping("/work-order/{workOrderId}")
    public ResponseEntity<ApiResponse<List<ProductionResult>>> getProductionResultsByWorkOrder(
            @PathVariable @Parameter(description = "작업 지시 ID") Long workOrderId) {
        List<ProductionResult> results = productionResultService.findByWorkOrderId(workOrderId);
        return ResponseEntity.ok(ApiResponse.success(results, "작업 지시별 생산 실적 조회 성공"));
    }

    @Operation(summary = "기간별 생산 실적 조회", description = "특정 기간의 생산 실적 목록을 조회합니다.")
    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<ProductionResult>>> getProductionResultsByPeriod(
            @RequestParam @Parameter(description = "시작 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @Parameter(description = "종료 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ProductionResult> results = productionResultService.findByPeriod(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(results, "기간별 생산 실적 조회 성공"));
    }

    @Operation(summary = "생산 실적 수정", description = "생산 실적 정보를 수정합니다. (ADMIN, MANAGER 권한 필요)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateProductionResult(
            @PathVariable @Parameter(description = "생산 실적 ID") Long id,
            @RequestParam(required = false) @Parameter(description = "생산 수량") Integer quantityProduced,
            @RequestParam(required = false) @Parameter(description = "불량 수량") Integer quantityDefective) {
        
        ProductionResult updateData = ProductionResult.builder()
                .quantityProduced(quantityProduced)
                .quantityDefective(quantityDefective)
                .build();
        
        productionResultService.updateProductionResult(id, updateData);
        return ResponseEntity.ok(ApiResponse.success(null, "생산 실적이 성공적으로 수정되었습니다."));
    }

    @Operation(summary = "생산 실적 삭제", description = "생산 실적을 삭제합니다. (ADMIN 권한 필요)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProductionResult(
            @PathVariable @Parameter(description = "생산 실적 ID") Long id) {
        productionResultService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent("생산 실적이 성공적으로 삭제되었습니다."));
    }

    @Operation(summary = "불량 등록", description = "기존 생산 실적에 추가 불량을 등록합니다. (MANAGER, OPERATOR 권한 필요)")
    @PostMapping("/{id}/defective")
    @PreAuthorize("hasRole('MANAGER') or hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<Void>> recordDefective(
            @PathVariable @Parameter(description = "생산 실적 ID") Long id,
            @RequestParam @Parameter(description = "추가 불량 수량") Integer additionalDefective) {
        productionResultService.recordDefective(id, additionalDefective);
        return ResponseEntity.ok(ApiResponse.success(null, "불량이 성공적으로 등록되었습니다."));
    }

    @Operation(summary = "생산 통계 조회", description = "특정 기간의 생산 통계를 조회합니다.")
    @GetMapping("/statistics/period")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductionStatistics(
            @RequestParam @Parameter(description = "시작 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @Parameter(description = "종료 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Map<String, Object> statistics = productionResultService.getProductionStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics, "생산 통계 조회 성공"));
    }

    @Operation(summary = "작업 지시별 통계 조회", description = "특정 작업 지시의 생산 통계를 조회합니다.")
    @GetMapping("/statistics/work-order/{workOrderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWorkOrderStatistics(
            @PathVariable @Parameter(description = "작업 지시 ID") Long workOrderId) {
        Map<String, Object> statistics = productionResultService.getWorkOrderStatistics(workOrderId);
        return ResponseEntity.ok(ApiResponse.success(statistics, "작업 지시별 통계 조회 성공"));
    }

    @Operation(summary = "일일 생산 통계 조회", description = "특정 날짜의 일일 생산 통계를 조회합니다.")
    @GetMapping("/statistics/daily")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDailyStatistics(
            @RequestParam @Parameter(description = "조회 날짜") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        Map<String, Object> statistics = productionResultService.getDailyStatistics(date);
        return ResponseEntity.ok(ApiResponse.success(statistics, "일일 생산 통계 조회 성공"));
    }

    @Operation(summary = "월별 생산 통계 조회", description = "특정 월의 생산 통계를 조회합니다.")
    @GetMapping("/statistics/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyStatistics(
            @RequestParam @Parameter(description = "연도") int year,
            @RequestParam @Parameter(description = "월") int month) {
        Map<String, Object> statistics = productionResultService.getMonthlyStatistics(year, month);
        return ResponseEntity.ok(ApiResponse.success(statistics, "월별 생산 통계 조회 성공"));
    }
}
