package com.smartfactory.smartmes_insight.domain.sensor;

import com.smartfactory.smartmes_insight.domain.facility.Facility;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sensors")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String type;

    private Double thresholdMin;
    private Double thresholdMax;

    @Column(length = 20)
    private String unit;
    
    // 🛡️ 안전한 업데이트 메서드들 (캡슐화)
    public void updateBasicInfo(String name, String type, String unit) {
        validateName(name);
        validateType(type);
        
        this.name = name;
        this.type = type;
        this.unit = unit;
    }

    //최소,최대 임계값 업뎃
    public void updateThresholds(Double thresholdMin, Double thresholdMax) {
        validateThresholds(thresholdMin, thresholdMax);
        
        this.thresholdMin = thresholdMin;
        this.thresholdMax = thresholdMax;
    }
    
    //비즈니스 검증 로직
    public boolean isValueWithinThreshold(Double value) {
        if (thresholdMin == null || thresholdMax == null || value == null) {
            return true; // 임계값이 설정되지 않은 경우 정상으로 간주
        }
        return value >= thresholdMin && value <= thresholdMax;
    }
    
    public boolean hasThresholds() {
        return thresholdMin != null && thresholdMax != null;
    }
    
    // 🛡️ 내부 검증 메서드들
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("센서명은 필수입니다");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("센서명은 100자를 초과할 수 없습니다");
        }
    }
    
    private void validateType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("센서 타입은 필수입니다");
        }
        // 허용된 센서 타입 검증
        if (!isValidSensorType(type)) {
            throw new IllegalArgumentException("지원하지 않는 센서 타입입니다: " + type);
        }
    }
    
    private void validateThresholds(Double min, Double max) {
        if (min != null && max != null && min >= max) {
            throw new IllegalArgumentException("최소 임계값은 최대 임계값보다 작아야 합니다");
        }
        if (min != null && min < 0) {
            throw new IllegalArgumentException("임계값은 0 이상이어야 합니다");
        }
    }
    
    private boolean isValidSensorType(String type) {
        return type.matches("TEMPERATURE|PRESSURE|VIBRATION|HUMIDITY|CURRENT|VOLTAGE");
    }
}