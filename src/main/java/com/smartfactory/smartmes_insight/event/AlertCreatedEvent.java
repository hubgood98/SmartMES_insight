package com.smartfactory.smartmes_insight.event;

import com.smartfactory.smartmes_insight.dto.AlertResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 알림 생성 이벤트
 * AlertService에서 알림이 생성될 때 발행되는 이벤트
 */
@Getter
@AllArgsConstructor
public class AlertCreatedEvent {
    
    private final AlertResponse alert;
    private final String severity;
    private final Long facilityId;
    private final Long sensorId;
    
    /**
     * 편의 생성자 - AlertResponse에서 필요한 정보 추출
     */
    public static AlertCreatedEvent from(AlertResponse alert) {
        return new AlertCreatedEvent(
            alert,
            alert.getSeverity(),
            alert.getFacilityId(),
            alert.getSensorId()
        );
    }
    
    /**
     * 이벤트 요약 정보
     */
    public String getSummary() {
        return String.format("[%s] 설비 %s에서 알림 발생", severity, alert.getFacilityName());
    }
    
    /**
     * 고심각도 알림인지 확인
     */
    public boolean isHighSeverity() {
        return "HIGH".equals(severity);
    }
    
    /**
     * 최근 알림인지 확인 (30분 이내)
     */
    public boolean isRecent() {
        return alert.isRecent();
    }
}
