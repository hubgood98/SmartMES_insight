package com.smartfactory.smartmes_insight.service;

import com.smartfactory.smartmes_insight.domain.production.ProductionResult;
import com.smartfactory.smartmes_insight.domain.production.ProductionResultRepository;
import com.smartfactory.smartmes_insight.domain.workorder.WorkOrder;
import com.smartfactory.smartmes_insight.domain.workorder.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductionResultService {

    private final ProductionResultRepository productionResultRepository;
    private final WorkOrderRepository workOrderRepository;

    // 작업 실적 등록
    public ProductionResult save(ProductionResult productionResult) {
        return productionResultRepository.save(productionResult);
    }

    // 작업 실적 등록 (작업 지시 ID로)
    public ProductionResult recordProduction(Long workOrderId, Integer quantityProduced, Integer quantityDefective) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("작업 지시를 찾을 수 없습니다."));

        ProductionResult productionResult = ProductionResult.builder()
                .workOrder(workOrder)
                .quantityProduced(quantityProduced)
                .quantityDefective(quantityDefective != null ? quantityDefective : 0)
                .recordedAt(LocalDateTime.now())
                .build();

        return productionResultRepository.save(productionResult);
    }

    // 불량 등록 (기존 실적에 불량 수량 추가)
    public void recordDefective(Long productionResultId, Integer additionalDefective) {
        ProductionResult productionResult = getProductionResultOrThrow(productionResultId);
        int currentDefective = productionResult.getQuantityDefective() != null ? productionResult.getQuantityDefective() : 0;
        productionResult.setQuantityDefective(currentDefective + additionalDefective);
    }

    // 전체 생산 실적 조회
    @Transactional(readOnly = true)
    public List<ProductionResult> findAll() {
        return productionResultRepository.findAll();
    }

    // 특정 작업 지시의 생산 실적 조회
    @Transactional(readOnly = true)
    public List<ProductionResult> findByWorkOrderId(Long workOrderId) {
        return productionResultRepository.findByWorkOrderId(workOrderId);
    }

    // 특정 기간의 생산 실적 조회
    @Transactional(readOnly = true)
    public List<ProductionResult> findByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return productionResultRepository.findByRecordedAtBetween(startDate, endDate);
    }

    // 생산 실적 단건 조회
    @Transactional(readOnly = true)
    public Optional<ProductionResult> findById(Long id) {
        return productionResultRepository.findById(id);
    }

    // 실적 통계 (총생산/불량률)
    @Transactional(readOnly = true)
    public Map<String, Object> getProductionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<ProductionResult> results = productionResultRepository.findByRecordedAtBetween(startDate, endDate);

        int totalProduced = results.stream()
                .mapToInt(ProductionResult::getQuantityProduced)
                .sum();

        int totalDefective = results.stream()
                .mapToInt(result -> result.getQuantityDefective() != null ? result.getQuantityDefective() : 0)
                .sum();

        double defectiveRate = totalProduced > 0 ? (double) totalDefective / totalProduced * 100 : 0.0;
        int goodQuantity = totalProduced - totalDefective;
        double goodRate = totalProduced > 0 ? (double) goodQuantity / totalProduced * 100 : 0.0;

        return Map.of(
                "totalProduced", totalProduced,
                "totalDefective", totalDefective,
                "goodQuantity", goodQuantity,
                "defectiveRate", Math.round(defectiveRate * 100.0) / 100.0,
                "goodRate", Math.round(goodRate * 100.0) / 100.0,
                "recordCount", results.size()
        );
    }

    // 특정 작업 지시의 통계
    @Transactional(readOnly = true)
    public Map<String, Object> getWorkOrderStatistics(Long workOrderId) {
        List<ProductionResult> results = productionResultRepository.findByWorkOrderId(workOrderId);

        int totalProduced = results.stream()
                .mapToInt(ProductionResult::getQuantityProduced)
                .sum();

        int totalDefective = results.stream()
                .mapToInt(result -> result.getQuantityDefective() != null ? result.getQuantityDefective() : 0)
                .sum();

        double defectiveRate = totalProduced > 0 ? (double) totalDefective / totalProduced * 100 : 0.0;

        return Map.of(
                "workOrderId", workOrderId,
                "totalProduced", totalProduced,
                "totalDefective", totalDefective,
                "goodQuantity", totalProduced - totalDefective,
                "defectiveRate", Math.round(defectiveRate * 100.0) / 100.0
        );
    }

    // 생산 실적 수정
    public void updateProductionResult(Long id, ProductionResult updatedResult) {
        ProductionResult productionResult = getProductionResultOrThrow(id);
        productionResult.setQuantityProduced(updatedResult.getQuantityProduced());
        productionResult.setQuantityDefective(updatedResult.getQuantityDefective());
    }

    // 생산 실적 삭제
    public void deleteById(Long id) {
        productionResultRepository.deleteById(id);
    }

    // 일별 생산 통계
    @Transactional(readOnly = true)
    public Map<String, Object> getDailyStatistics(LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        
        return getProductionStatistics(startOfDay, endOfDay);
    }

    // 월별 생산 통계
    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyStatistics(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        
        return getProductionStatistics(startOfMonth, endOfMonth);
    }

    private ProductionResult getProductionResultOrThrow(Long id) {
        return productionResultRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("생산 실적을 찾을 수 없습니다."));
    }
}
