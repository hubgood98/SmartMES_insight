package com.smartfactory.smartmes_insight.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorUpdateRequest {
    
    @Size(min = 2, max = 100, message = "센서명은 2-100자 사이여야 합니다")
    private String name;
    
    @Size(min = 1, max = 50, message = "센서 타입은 1-50자 사이여야 합니다")
    private String type;
    
    @Size(max = 20, message = "단위는 20자를 초과할 수 없습니다")
    private String unit;
    
    @DecimalMin(value = "0.0", message = "최소 임계값은 0 이상이어야 합니다")
    private Double thresholdMin;
    
    @DecimalMin(value = "0.0", message = "최대 임계값은 0 이상이어야 합니다")
    private Double thresholdMax;
    
    // 🎯 선택적 업데이트를 위한 헬퍼 메서드들
    public boolean hasBasicInfo() {
        return name != null || type != null || unit != null;
    }
    
    public boolean hasThresholds() {
        return thresholdMin != null || thresholdMax != null;
    }
    
    // 비즈니스 검증 로직
    public void validate() {
        if (thresholdMin != null && thresholdMax != null && thresholdMin >= thresholdMax) {
            throw new IllegalArgumentException("최소 임계값은 최대 임계값보다 작아야 합니다");
        }
    }
}
