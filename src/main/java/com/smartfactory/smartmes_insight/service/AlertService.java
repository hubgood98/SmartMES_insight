package com.smartfactory.smartmes_insight.service;

import com.smartfactory.smartmes_insight.domain.alert.Alert;
import com.smartfactory.smartmes_insight.domain.alert.AlertRepository;
import com.smartfactory.smartmes_insight.domain.sensor.Sensor;
import com.smartfactory.smartmes_insight.domain.sensor.SensorRepository;
import com.smartfactory.smartmes_insight.dto.AlertResponse;
import com.smartfactory.smartmes_insight.event.AlertCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;
    private final SensorRepository sensorRepository;
    private final SensorService sensorService;
    private final ApplicationEventPublisher eventPublisher; // 🎯 이벤트 발행용

    // 알림 생성
    public AlertResponse createAlert(Long sensorId, Double value, String message) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new IllegalArgumentException("센서를 찾을 수 없습니다."));

        Alert alert = Alert.builder()
                .sensor(sensor)
                .value(value)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        Alert savedAlert = alertRepository.save(alert);
        AlertResponse alertResponse = AlertResponse.from(savedAlert);
        
        // 🚀 이벤트 발행 (비동기 알림 처리)
        eventPublisher.publishEvent(AlertCreatedEvent.from(alertResponse));
        
        return alertResponse;
    }

    // 센서 값이 임계값을 벗어났을 때 자동 알림 생성
    public Optional<AlertResponse> checkAndCreateAlert(Long sensorId, Double value) {
        // 🛡️ SensorService의 안전한 메서드 사용
        if (!sensorService.hasThresholds(sensorId)) {
            return Optional.empty(); // 임계값이 설정되지 않은 경우 알림 생성하지 않음
        }
        
        if (!sensorService.isValueWithinThreshold(sensorId, value)) {
            // 센서 정보를 안전하게 조회
            Sensor sensor = sensorService.findEntityById(sensorId);
                
            String message = String.format(
                "센서 '%s'에서 이상값 감지: %.2f (임계값: %.2f - %.2f)",
                sensor.getName(), value, sensor.getThresholdMin(), sensor.getThresholdMax()
            );
            AlertResponse alertResponse = createAlert(sensorId, value, message);
            return Optional.of(alertResponse);
        }
        
        return Optional.empty();
    }

    // 전체 알림 목록 조회
    @Transactional(readOnly = true)
    public List<AlertResponse> findAll() {
        return alertRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AlertResponse::from)
                .collect(Collectors.toList());
    }

    // 특정 센서의 알림 조회
    @Transactional(readOnly = true)
    public List<AlertResponse> findBySensorId(Long sensorId) {
        return alertRepository.findBySensorIdOrderByCreatedAtDesc(sensorId)
                .stream()
                .map(AlertResponse::from)
                .collect(Collectors.toList());
    }

    // 특정 기간의 알림 조회
    @Transactional(readOnly = true)
    public List<AlertResponse> findByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return alertRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate)
                .stream()
                .map(AlertResponse::from)
                .collect(Collectors.toList());
    }

    // 최근 알림 조회 (개수 제한)
    @Transactional(readOnly = true)
    public List<AlertResponse> findRecentAlerts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return alertRepository.findRecentAlerts(pageable)
                .stream()
                .map(AlertResponse::from)
                .collect(Collectors.toList());
    }

    // 알림 단건 조회
    @Transactional(readOnly = true)
    public Optional<AlertResponse> findById(Long id) {
        return alertRepository.findById(id)
                .map(AlertResponse::from);
    }

    // 심각도별 알림 조회
    @Transactional(readOnly = true)
    public List<AlertResponse> findBySeverity(String severity) {
        return findAll().stream()
                .filter(alert -> severity.equals(alert.getSeverity()))
                .collect(Collectors.toList());
    }

    // 최근 알림만 필터링 (30분 이내)
    @Transactional(readOnly = true)
    public List<AlertResponse> findRecentAlertsOnly() {
        return findAll().stream()
                .filter(AlertResponse::isRecent)
                .collect(Collectors.toList());
    }

    // 설비별 알림 통계
    @Transactional(readOnly = true)
    public List<AlertResponse> findByFacilityId(Long facilityId) {
        return findAll().stream()
                .filter(alert -> facilityId.equals(alert.getFacilityId()))
                .collect(Collectors.toList());
    }

    // 알림 요약 정보 리스트
    @Transactional(readOnly = true)
    public List<String> getAlertSummaries(int limit) {
        return findRecentAlerts(limit).stream()
                .map(AlertResponse::getSummary)
                .collect(Collectors.toList());
    }

    // ========================= Entity 기반 메서드들 (내부 사용용) =========================
    
    // 🔒 내부 사용 - Entity 저장
    private Alert saveEntity(Alert alert) {
        return alertRepository.save(alert);
    }

    // 🔒 내부 사용 - Entity 조회
    @Transactional(readOnly = true)
    public Optional<Alert> findEntityById(Long id) {
        return alertRepository.findById(id);
    }

    // ========================= 삭제 관련 메서드들 =========================
    
    // 알림 삭제
    public void deleteById(Long id) {
        alertRepository.deleteById(id);
    }

    // 특정 센서의 알림 전체 삭제
    public void deleteBySensorId(Long sensorId) {
        alertRepository.deleteBySensorId(sensorId);
    }

    // ✅ 새로운 삭제 메서드 - 오래된 알림 정리 (예: 30일 이상)
    public void deleteOldAlerts(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        alertRepository.deleteByCreatedAtBefore(cutoffDate);
    }

    // TODO: WebSocket 알림 전송 기능 (확장용)
    // public void sendWebSocketAlert(AlertResponse alertResponse) {
    //     // WebSocket을 통한 실시간 알림 전송 로직
    // }
}
