package com.smartfactory.smartmes_insight.dto;

import com.smartfactory.smartmes_insight.domain.sensor.Sensor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorResponse {
    
    private Long id;
    private String name;
    private String type;
    private Double thresholdMin;
    private Double thresholdMax;
    private String unit;
    private Long facilityId;
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
