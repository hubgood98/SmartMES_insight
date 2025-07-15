package com.smartfactory.smartmes_insight.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorCreateRequest {
    
    @NotNull(message = "설비 ID는 필수입니다")
    private Long facilityId;
    
    @NotBlank(message = "센서명은 필수입니다")
    private String name;
    
    @NotBlank(message = "센서 타입은 필수입니다")
    private String type;
    
    @DecimalMin(value = "0.0", message = "최소 임계값은 0 이상이어야 합니다")
    private Double thresholdMin;
    
    @DecimalMin(value = "0.0", message = "최대 임계값은 0 이상이어야 합니다")
    private Double thresholdMax;
    
    private String unit;
    
    // 비즈니스 검증 로직
    public void validate() {
        if (thresholdMin != null && thresholdMax != null && thresholdMin >= thresholdMax) {
            throw new IllegalArgumentException("최소 임계값은 최대 임계값보다 작아야 합니다");
        }
    }
}
