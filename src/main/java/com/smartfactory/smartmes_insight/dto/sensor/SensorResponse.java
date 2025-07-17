package com.smartfactory.smartmes_insight.dto.sensor;

import com.smartfactory.smartmes_insight.domain.sensor.Sensor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "센서 응답 DTO")
public class SensorResponse {
    
    @Schema(description = "센서 ID", example = "1")
    private Long id;
    
    @Schema(description = "센서명", example = "온도 센서 #1")
    private String name;
    
    @Schema(description = "센서 타입", example = "TEMPERATURE")
    private String type;
    
    @Schema(description = "최소 임계값", example = "60.0")
    private Double thresholdMin;
    
    @Schema(description = "최대 임계값", example = "80.0")
    private Double thresholdMax;
    
    @Schema(description = "측정 단위", example = "°C")
    private String unit;
    
    @Schema(description = "설비 ID", example = "1")
    private Long facilityId;
    
    @Schema(description = "설비명", example = "CNC 가공기 #1")
    private String facilityName;
    
    // Entity -> DTO 변환
    public static SensorResponse from(Sensor sensor) {
        return SensorResponse.builder()
                .id(sensor.getId())
                .name(sensor.getName())
                .type(sensor.getType())
                .thresholdMin(sensor.getThresholdMin())
                .thresholdMax(sensor.getThresholdMax())
                .unit(sensor.getUnit())
                .facilityId(sensor.getFacility().getId())
                .facilityName(sensor.getFacility().getName())
                .build();
    }
}
