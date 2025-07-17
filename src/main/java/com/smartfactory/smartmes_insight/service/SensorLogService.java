package com.smartfactory.smartmes_insight.service;

import com.smartfactory.smartmes_insight.domain.sensor.Sensor;
import com.smartfactory.smartmes_insight.domain.sensor.SensorLog;
import com.smartfactory.smartmes_insight.domain.sensor.SensorLogRepository;
import com.smartfactory.smartmes_insight.domain.sensor.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SensorLogService {

    private final SensorLogRepository sensorLogRepository;
    private final SensorRepository sensorRepository;

    // 실시간 센서 데이터 저장
    public SensorLog save(SensorLog sensorLog) {
        return sensorLogRepository.save(sensorLog);
    }

    // 센서 데이터 저장 (센서 ID와 값으로)
    public SensorLog saveSensorData(Long sensorId, Double value) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new IllegalArgumentException("센서를 찾을 수 없습니다."));

        SensorLog sensorLog = SensorLog.builder()
                .sensor(sensor)
                .value(value)
                .collectedAt(LocalDateTime.now())
                .build();

        return sensorLogRepository.save(sensorLog);
    }
    
    // 센서 데이터 로깅 (스케줄러용 별칭)
    public SensorLog logSensorData(Long sensorId, Double value) {
        return saveSensorData(sensorId, value);
    }

    // 특정 기간 센서 로그 조회
    @Transactional(readOnly = true)
    public List<SensorLog> findByPeriod(Long sensorId, LocalDateTime startDate, LocalDateTime endDate) {
        return sensorLogRepository.findBySensorIdAndCollectedAtBetween(sensorId, startDate, endDate);
    }

    // 특정 센서의 최근 로그 조회
    @Transactional(readOnly = true)
    public List<SensorLog> findRecentLogsBySensorId(Long sensorId, int limit) {
        return sensorLogRepository.findTop10BySensorIdOrderByCollectedAtDesc(sensorId);
    }

    // 이상 패턴 탐지 (임계값 기반)
    @Transactional(readOnly = true)
    public List<SensorLog> detectAnomalies(Long sensorId, LocalDateTime startDate, LocalDateTime endDate) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new IllegalArgumentException("센서를 찾을 수 없습니다."));

        if (sensor.getThresholdMin() == null || sensor.getThresholdMax() == null) {
            throw new IllegalArgumentException("센서의 임계값이 설정되지 않았습니다.");
        }

        return sensorLogRepository.findBySensorIdAndCollectedAtBetweenAndValueNotBetween(
                sensorId, startDate, endDate, sensor.getThresholdMin(), sensor.getThresholdMax()
        );
    }

    // 로그 데이터 통계 제공
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics(Long sensorId, LocalDateTime startDate, LocalDateTime endDate) {
        List<SensorLog> logs = sensorLogRepository.findBySensorIdAndCollectedAtBetween(sensorId, startDate, endDate);
        
        if (logs.isEmpty()) {
            return Map.of(
                "count", 0,
                "average", 0.0,
                "min", 0.0,
                "max", 0.0
            );
        }

        List<Double> values = logs.stream()
                .map(SensorLog::getValue)
                .collect(Collectors.toList());

        double average = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        return Map.of(
                "count", logs.size(),
                "average", average,
                "min", min,
                "max", max
        );
    }

    // 전체 센서의 최신 데이터 조회
    @Transactional(readOnly = true)
    public List<SensorLog> findLatestDataForAllSensors() {
        return sensorLogRepository.findLatestDataForAllSensors();
    }
}
