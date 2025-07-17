package com.smartfactory.smartmes_insight.scheduler;

import com.smartfactory.smartmes_insight.service.AlertService;
import com.smartfactory.smartmes_insight.service.SensorLogService;
import com.smartfactory.smartmes_insight.service.SensorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * 센서 모니터링 스케줄러
 * 실시간 센서 데이터 수집 및 알림 체크
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SensorMonitoringScheduler {

    private final SensorService sensorService;
    private final SensorLogService sensorLogService;
    private final AlertService alertService;
    private final Random random = new Random();

    /**
     * 실시간 센서 데이터 수집 및 모니터링
     * 10초마다 실행 (프로젝트 문서 명시)
     */
    @Async
    @Scheduled(fixedRate = 10000) // 10초마다 실행
    public void monitorSensors() {
        try {
            // 활성화된 모든 센서 조회
            List<Long> activeSensorIds = sensorService.findActiveSensorIds();
            
            if (activeSensorIds.isEmpty()) {
                log.debug("활성화된 센서가 없습니다.");
                return;
            }
            
            log.debug("센서 모니터링 시작 - 대상 센서: {}개", activeSensorIds.size());
            
            // 각 센서별 데이터 수집 및 처리
            for (Long sensorId : activeSensorIds) {
                try {
                    processSensorData(sensorId);
                } catch (Exception e) {
                    log.error("센서 {} 데이터 처리 중 오류: {}", sensorId, e.getMessage());
                }
            }
            
            log.debug("센서 모니터링 완료");
            
        } catch (Exception e) {
            log.error("센서 모니터링 스케줄러 실행 중 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 개별 센서 데이터 처리
     * @param sensorId 센서 ID
     */
    private void processSensorData(Long sensorId) {
        try {
            // 1. 센서 데이터 수집 (실제 환경에서는 하드웨어에서 읽어옴)
            Double sensorValue = collectSensorData(sensorId);
            
            // 2. 센서 로그 저장
            sensorLogService.logSensorData(sensorId, sensorValue);
            
            // 3. 알림 체크 및 생성
            alertService.checkAndCreateAlert(sensorId, sensorValue);
            
            log.trace("센서 {} 데이터 처리 완료: {}", sensorId, sensorValue);
            
        } catch (Exception e) {
            log.error("센서 {} 데이터 처리 실패: {}", sensorId, e.getMessage());
        }
    }

    /**
     * 센서 데이터 수집 시뮬레이션
     * 실제 환경에서는 하드웨어 API 호출
     * @param sensorId 센서 ID
     * @return 센서 값
     */
    private Double collectSensorData(Long sensorId) {
        // TODO: 실제 센서 하드웨어 API 연동 시 교체
        // 현재는 시뮬레이션 데이터 생성
        
        // 센서 타입별 시뮬레이션 데이터 생성
        String sensorType = getSensorType(sensorId);
        
        switch (sensorType) {
            case "TEMPERATURE":
                // 온도: 50~100°C (가끔 임계값 초과)
                return 50.0 + random.nextDouble() * 50.0 + (random.nextInt(100) < 5 ? 20.0 : 0.0);
                
            case "PRESSURE":
                // 압력: 1~10bar (가끔 임계값 초과)
                return 1.0 + random.nextDouble() * 9.0 + (random.nextInt(100) < 3 ? 5.0 : 0.0);
                
            case "VIBRATION":
                // 진동: 0~5Hz (가끔 임계값 초과)
                return random.nextDouble() * 5.0 + (random.nextInt(100) < 7 ? 3.0 : 0.0);
                
            case "HUMIDITY":
                // 습도: 30~80% (가끔 임계값 초과)
                return 30.0 + random.nextDouble() * 50.0 + (random.nextInt(100) < 4 ? 15.0 : 0.0);
                
            default:
                // 기본값: 0~100
                return random.nextDouble() * 100.0;
        }
    }

    /**
     * 센서 타입 조회 (캐시 또는 DB에서)
     * @param sensorId 센서 ID
     * @return 센서 타입
     */
    private String getSensorType(Long sensorId) {
        // TODO: 실제로는 SensorService에서 조회
        // 현재는 시뮬레이션용 타입 반환
        String[] types = {"TEMPERATURE", "PRESSURE", "VIBRATION", "HUMIDITY"};
        return types[(int) (sensorId % types.length)];
    }

    /**
     * 센서 모니터링 통계 로깅 (5분마다)
     */
    @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void logMonitoringStatistics() {
        try {
            List<Long> activeSensorIds = sensorService.findActiveSensorIds();
            
            log.info("=== 센서 모니터링 통계 ===");
            log.info("활성 센서 수: {}", activeSensorIds.size());
            log.info("최근 알림 수: {}", alertService.findRecentAlertsOnly().size());
            log.info("============================");
            
        } catch (Exception e) {
            log.error("센서 모니터링 통계 로깅 실패: {}", e.getMessage());
        }
    }
}
