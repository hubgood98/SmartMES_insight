package com.smartfactory.smartmes_insight.dto;

import com.smartfactory.smartmes_insight.domain.alert.Alert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertResponse {
    
    private Long id;
    private Double value;
    private String message;
    private LocalDateTime createdAt;
    
    // 센서 정보 (연관 엔티티 대신 필요한 정보만)
    private Long sensorId;
    private String sensorName;
    private String sensorType;
    private String sensorUnit;
    
    // 설비 정보 (센서를 통한 간접 참조)
    private Long facilityId;
    private String facilityName;
    
    // 임계값 정보 (알림 발생 당시의 기준)
    private Double thresholdMin;
    private Double thresholdMax;
    
    // Entity -> DTO 변환
    public static AlertResponse from(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .value(alert.getValue())
                .message(alert.getMessage())
                .createdAt(alert.getCreatedAt())
                
                // 센서 정보
                .sensorId(alert.getSensor().getId())
                .sensorName(alert.getSensor().getName())
                .sensorType(alert.getSensor().getType())
                .sensorUnit(alert.getSensor().getUnit())
                
                // 설비 정보 (센서를 통해 접근)
                .facilityId(alert.getSensor().getFacility().getId())
                .facilityName(alert.getSensor().getFacility().getName())
                
                // 임계값 정보 (알림 발생 당시)
                .thresholdMin(alert.getSensor().getThresholdMin())
                .thresholdMax(alert.getSensor().getThresholdMax())
                .build();
    }
    
    // 🎯 비즈니스 메서드들
    
    /**
     * 알림 심각도 계산
     */
    public String getSeverity() {
        if (thresholdMin == null || thresholdMax == null) {
            return "UNKNOWN";
        }
        
        double range = thresholdMax - thresholdMin;
        double deviation;
        
        if (value < thresholdMin) {
            deviation = Math.abs(thresholdMin - value);
        } else if (value > thresholdMax) {
            deviation = Math.abs(value - thresholdMax);
        } else {
            return "NORMAL";  // 임계값 내부 (정상)
        }
        
        // 임계값 범위의 50% 이상 벗어나면 HIGH, 그렇지 않으면 MEDIUM
        if (deviation > range * 0.5) {
            return "HIGH";
        } else {
            return "MEDIUM";
        }
    }
    
    /**
     * 알림이 얼마나 오래되었는지 확인 (분 단위)
     */
    public long getAgeInMinutes() {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
    }
    
    //최근 알림인지 확인 (30분 이내)
    public boolean isRecent() {
        return getAgeInMinutes() <= 30;
    }
    
    //임계값 정보 포맷팅
    public String getThresholdInfo() {
        if (thresholdMin == null || thresholdMax == null) {
            return "임계값 미설정";
        }
        return String.format("%.2f - %.2f %s", thresholdMin, thresholdMax, 
                            sensorUnit != null ? sensorUnit : "");
    }
    
    //알림 요약 정보
    public String getSummary() {
        return String.format("[%s] %s에서 %s 알림 발생 (값: %.2f)", 
                            getSeverity(), facilityName, sensorName, value);
    }
}
