package com.smartfactory.smartmes_insight.event;

import com.smartfactory.smartmes_insight.dto.AlertResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 알림 이벤트 핸들러
 * 비동기로 알림 처리 (로깅, 이메일, 웹소켓 등)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertEventHandler {

    // TODO: 추후 WebSocket, Email 서비스 주입 예정
    // private final WebSocketService webSocketService;
    // private final EmailService emailService;

    /**
     * 알림 생성 이벤트 처리 - 비동기 실행
     * @param event 알림 생성 이벤트
     */
    @Async
    @EventListener
    public void handleAlertCreated(AlertCreatedEvent event) {
        try {
            AlertResponse alert = event.getAlert();
            
            // 1. 로깅 처리
            logAlert(alert, event.getSeverity());
            
            // 2. 고심각도 알림인 경우 추가 처리
            if (event.isHighSeverity()) {
                handleHighSeverityAlert(alert);
            }
            
            // 3. 실시간 알림 처리 (WebSocket)
            // sendWebSocketAlert(alert);
            
            // 4. 이메일 알림 처리 (선택사항)
            // sendEmailAlert(alert);
            
            log.info("알림 이벤트 처리 완료: {}", alert.getSummary());
            
        } catch (Exception e) {
            log.error("알림 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 알림 로깅 처리
     */
    private void logAlert(AlertResponse alert, String severity) {
        String logMessage = String.format(
            "🚨 [%s] 알림 발생 - 설비: %s, 센서: %s, 값: %.2f, 시간: %s",
            severity,
            alert.getFacilityName(),
            alert.getSensorName(),
            alert.getValue(),
            alert.getCreatedAt()
        );
        
        // 심각도에 따른 로그 레벨 구분
        switch (severity) {
            case "HIGH":
                log.error(logMessage);
                break;
            case "MEDIUM":
                log.warn(logMessage);
                break;
            default:
                log.info(logMessage);
        }
    }

    /**
     * 고심각도 알림 추가 처리
     */
    private void handleHighSeverityAlert(AlertResponse alert) {
        log.error("HIGH 심각도 알림 - 즉시 대응 필요! 설비: {}, 메시지: {}",
                 alert.getFacilityName(), alert.getMessage());
        
        // TODO: 고심각도 알림 처리 로직
        // - 관리자 즉시 알림
        // - 자동 작업 중단 검토
        // - 비상 연락망 가동
    }

    /**
     * WebSocket을 통한 실시간 알림 전송 (추후 구현)
     */
    private void sendWebSocketAlert(AlertResponse alert) {
        // TODO: WebSocket 구현 시 활성화
        // webSocketService.sendAlert(alert);
        log.debug("WebSocket 알림 전송 예정: {}", alert.getSummary());
    }

    /**
     * 이메일 알림 전송 (추후 구현)
     */
    private void sendEmailAlert(AlertResponse alert) {
        // TODO: Email 구현 시 활성화
        // emailService.sendAlert(alert);
        log.debug("이메일 알림 전송 예정: {}", alert.getSummary());
    }
}
