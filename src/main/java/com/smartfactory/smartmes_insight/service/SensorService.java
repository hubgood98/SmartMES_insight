package com.smartfactory.smartmes_insight.service;

import com.smartfactory.smartmes_insight.domain.sensor.Sensor;
import com.smartfactory.smartmes_insight.domain.sensor.SensorRepository;
import com.smartfactory.smartmes_insight.domain.facility.Facility;
import com.smartfactory.smartmes_insight.dto.SensorResponse;
import com.smartfactory.smartmes_insight.dto.SensorCreateRequest;
import com.smartfactory.smartmes_insight.dto.SensorUpdateRequest;
import com.smartfactory.smartmes_insight.dto.SensorSettingsRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SensorService {

    private final SensorRepository sensorRepository;
    private final FacilityService facilityService;

    //센서 등록
    public SensorResponse createSensor(SensorCreateRequest request) {
        // 1. 입력 검증
        validateSensorRequest(request);
        // 2. 설비 존재 확인
        Facility facility = facilityService.findEntityById(request.getFacilityId());
        // 3. 센서 엔티티 생성 및 검증
        Sensor sensor = buildSensorFromRequest(request, facility);
        // 4. 저장 및 반환
        Sensor saved = sensorRepository.save(sensor);
        return SensorResponse.from(saved);
    }

    public SensorResponse updateSensor(Long id, SensorUpdateRequest request) {
        // 1. 입력 검증
        request.validate();
        // 2. 센서 조회
        Sensor sensor = findSensorOrThrow(id);
        // 3. 요청에 포함된 필드만 선택적 업데이트
        if (request.hasBasicInfo()) {
            sensor.updateBasicInfo(
                request.getName() != null ? request.getName() : sensor.getName(),
                request.getType() != null ? request.getType() : sensor.getType(),
                request.getUnit() != null ? request.getUnit() : sensor.getUnit()
            );
        }
        
        if (request.hasThresholds()) {
            sensor.updateThresholds(request.getThresholdMin(), request.getThresholdMax());
        }
        // 4. 반환
        return SensorResponse.from(sensor);
    }

    public void deleteSensor(Long id) {
        // 1. 존재 확인
        if (!sensorRepository.existsById(id)) {
            throw new EntityNotFoundException("센서를 찾을 수 없습니다: " + id);
        }
        
        // 2. 삭제 (연관 데이터 정리는 DB 제약조건 활용)
        sensorRepository.deleteById(id);
    }

    // 센서 목록 조회
    @Transactional(readOnly = true)
    public List<SensorResponse> getAllSensors() {
        return sensorRepository.findAll()
                .stream()
                .map(SensorResponse::from)
                .toList();
    }

    // 3️⃣ 특정 설비에 연결된 센서 조회
    
    /**
     * 설비별 센서 조회
     */
    @Transactional(readOnly = true)
    public List<SensorResponse> getSensorsByFacility(Long facilityId) {
        return sensorRepository.findByFacilityId(facilityId)
                .stream()
                .map(SensorResponse::from)
                .toList();
    }

    // 4️⃣ 센서 단위 세부 설정값 저장 (threshold 등)
    
    /**
     * 센서 설정 전용 업데이트
     */
    public SensorResponse updateSensorSettings(Long sensorId, SensorSettingsRequest request) {
        // 1. 입력 검증
        request.validate();
        // 2. 센서 조회
        Sensor sensor = findSensorOrThrow(sensorId);
        // 3. 설정값만 업데이트
        sensor.updateThresholds(request.getThresholdMin(), request.getThresholdMax());
        // 4. 추가 설정들 (향후 확장 가능)
        // sensor.updateSamplingRate(request.getSamplingRate());
        // sensor.updateCalibration(request.getCalibrationValue());
        
        return SensorResponse.from(sensor);
    }
    /**
     * 센서 조회 또는 예외 발생
     */
    private Sensor findSensorOrThrow(Long sensorId) {
        return sensorRepository.findById(sensorId)
                .orElseThrow(() -> new EntityNotFoundException("센서를 찾을 수 없습니다: " + sensorId));
    }
    
    /**
     * 센서 생성 요청 검증
     */
    private void validateSensorRequest(SensorCreateRequest request) {
        if (request.getFacilityId() == null) {
            throw new IllegalArgumentException("설비 ID는 필수입니다");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("센서명은 필수입니다");
        }
        if (!StringUtils.hasText(request.getType())) {
            throw new IllegalArgumentException("센서 타입은 필수입니다");
        }
        
        // 임계값 검증은 엔티티에서 처리
        request.validate();
    }
    
    /**
     * 요청으로부터 센서 엔티티 생성
     */
    private Sensor buildSensorFromRequest(SensorCreateRequest request, Facility facility) {
        Sensor sensor = Sensor.builder()
                .facility(facility)
                .name(request.getName())
                .type(request.getType())
                .unit(request.getUnit())
                .build();
        
        // 엔티티 검증 활용
        sensor.updateBasicInfo(request.getName(), request.getType(), request.getUnit());
        
        if (request.getThresholdMin() != null || request.getThresholdMax() != null) {
            sensor.updateThresholds(request.getThresholdMin(), request.getThresholdMax());
        }
        
        return sensor;
    }
    
    // 🔍 다른 서비스에서 필요한 경우에만 사용하는 메서드들
    
    /**
     * 센서 엔티티 직접 조회 (서비스간 통신용)
     */
    @Transactional(readOnly = true)
    public Sensor findEntityById(Long id) {
        return findSensorOrThrow(id);
    }
    
    /**
     * 센서 값 임계값 검증 (비즈니스 로직)
     */
    @Transactional(readOnly = true)
    public boolean isValueWithinThreshold(Long sensorId, Double value) {
        Sensor sensor = findSensorOrThrow(sensorId);
        return sensor.isValueWithinThreshold(value);
    }
    
    /**
     * 임계값 설정 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean hasThresholds(Long sensorId) {
        Sensor sensor = findSensorOrThrow(sensorId);
        return sensor.hasThresholds();
    }
    
    /**
     * 활성화된 센서 ID 목록 조회 (스케줄러용)
     */
    @Transactional(readOnly = true)
    public List<Long> findActiveSensorIds() {
        return sensorRepository.findAllActiveSensorIds();
    }
}
