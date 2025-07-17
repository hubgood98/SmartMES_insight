package com.smartfactory.smartmes_insight.dto.sensor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "센서 설정 요청 DTO")
public class SensorSettingsRequest {
    
    // 임계값 설정
    @DecimalMin(value = "0.0", message = "최소 임계값은 0 이상이어야 합니다")
    @Schema(description = "최소 임계값", example = "60.0")
    private Double thresholdMin;
    
    @DecimalMin(value = "0.0", message = "최대 임계값은 0 이상이어야 합니다")
    @Schema(description = "최대 임계값", example = "80.0")
    private Double thresholdMax;
    
    // 🔧 향후 확장 가능한 설정들 (주석 처리)
    // private Integer samplingRate;        // 샘플링 주기 (초)
    // private Double calibrationValue;     // 보정값
    // private Boolean alertEnabled;        // 알림 활성화 여부
    // private String alertLevel;           // 알림 레벨 (LOW, MEDIUM, HIGH)
    // private Integer dataRetentionDays;   // 데이터 보관 기간 (일)
    
    // 🔍 비즈니스 검증 로직
    public void validate() {
        if (thresholdMin != null && thresholdMax != null && thresholdMin >= thresholdMax) {
            throw new IllegalArgumentException("최소 임계값은 최대 임계값보다 작아야 합니다");
        }
    }
    
    // 🎯 헬퍼 메서드들
    public boolean hasThresholds() {
        return thresholdMin != null || thresholdMax != null;
    }
    
    public boolean hasValidThresholds() {
        return thresholdMin != null && thresholdMax != null && thresholdMin < thresholdMax;
    }
    
    public boolean isEmpty() {
        return thresholdMin == null && thresholdMax == null;
        // 향후 다른 설정 필드 추가 시 여기에 추가
    }
}
