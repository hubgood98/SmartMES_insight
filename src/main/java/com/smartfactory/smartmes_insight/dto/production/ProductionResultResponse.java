package com.smartfactory.smartmes_insight.dto.production;

import com.smartfactory.smartmes_insight.domain.production.ProductionResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "생산 실적 응답 DTO")
public class ProductionResultResponse {

    @Schema(description = "생산 실적 ID", example = "1")
    private Long id;

    @Schema(description = "생산 수량", example = "950")
    private Integer quantityProduced;

    @Schema(description = "불량 수량", example = "15")
    private Integer quantityDefective;

    @Schema(description = "양품 수량", example = "935")
    private Integer quantityGood;

    @Schema(description = "불량률", example = "1.58")
    private Double defectiveRate;

    @Schema(description = "양품률", example = "98.42")
    private Double goodRate;

    @Schema(description = "실적 등록 시간", example = "2025-01-20T18:00:00")
    private LocalDateTime recordedAt;

    @Schema(description = "생산 실적 메모", example = "야간 작업으로 인한 약간의 품질 저하")
    private String memo;

    // 작업 지시 정보
    @Schema(description = "작업 지시 ID", example = "1")
    private Long workOrderId;

    @Schema(description = "제품명", example = "자동차 부품 A")
    private String productName;

    @Schema(description = "목표 수량", example = "1000")
    private Integer targetQuantity;

    @Schema(description = "달성률", example = "95.0")
    private Double achievementRate;

    // 설비 정보
    @Schema(description = "설비 ID", example = "1")
    private Long facilityId;

    @Schema(description = "설비명", example = "CNC 가공기 #1")
    private String facilityName;

    @Schema(description = "설비 타입", example = "CNC")
    private String facilityType;

    public static ProductionResultResponse from(ProductionResult result) {
        int quantityGood = result.getQuantityProduced() - (result.getQuantityDefective() != null ? result.getQuantityDefective() : 0);
        double defectiveRate = result.getQuantityProduced() > 0 ? 
            (double) (result.getQuantityDefective() != null ? result.getQuantityDefective() : 0) / result.getQuantityProduced() * 100 : 0.0;
        double goodRate = 100.0 - defectiveRate;
        double achievementRate = result.getWorkOrder().getQuantity() > 0 ? 
            (double) result.getQuantityProduced() / result.getWorkOrder().getQuantity() * 100 : 0.0;

        return ProductionResultResponse.builder()
                .id(result.getId())
                .quantityProduced(result.getQuantityProduced())
                .quantityDefective(result.getQuantityDefective())
                .quantityGood(quantityGood)
                .defectiveRate(Math.round(defectiveRate * 100.0) / 100.0)
                .goodRate(Math.round(goodRate * 100.0) / 100.0)
                .recordedAt(result.getRecordedAt())
                .memo(result.getMemo())
                .workOrderId(result.getWorkOrder().getId())
                .productName(result.getWorkOrder().getProductName())
                .targetQuantity(result.getWorkOrder().getQuantity())
                .achievementRate(Math.round(achievementRate * 100.0) / 100.0)
                .facilityId(result.getWorkOrder().getFacility().getId())
                .facilityName(result.getWorkOrder().getFacility().getName())
                .facilityType(result.getWorkOrder().getFacility().getType())
                .build();
    }

    /**
     * 생산 품질 등급 계산
     */
    @Schema(description = "품질 등급", example = "GOOD")
    public String getQualityGrade() {
        if (defectiveRate == null) return "UNKNOWN";
        
        if (defectiveRate <= 1.0) return "EXCELLENT";
        else if (defectiveRate <= 3.0) return "GOOD";
        else if (defectiveRate <= 5.0) return "AVERAGE";
        else return "POOR";
    }

    /**
     * 생산 효율성 평가
     */
    @Schema(description = "효율성 평가", example = "HIGH")
    public String getEfficiencyLevel() {
        if (achievementRate == null) return "UNKNOWN";
        
        if (achievementRate >= 95.0) return "HIGH";
        else if (achievementRate >= 85.0) return "MEDIUM";
        else return "LOW";
    }
}
