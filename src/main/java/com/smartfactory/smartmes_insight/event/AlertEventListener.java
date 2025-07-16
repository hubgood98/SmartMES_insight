package com.smartfactory.smartmes_insight.event;

import com.smartfactory.smartmes_insight.dto.AlertResponse;
import com.smartfactory.smartmes_insight.service.NotificationService;
import com.smartfactory.smartmes_insight.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 알림 이벤트 처리 전담 리스너
 * AlertService에서 발행된 이벤트를 받아 다양한 알림 채널로 전송
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertEventListener {
    
    private final NotificationService notificationService;
    private final UserService userService;
    
    /**
     * 알림 생성 이벤트 처리 (메인 핸들러)
     */
    @EventListener
    @Async("notificationExecutor") // 🚀 전용 스레드 풀 사용
    public void handleAlertCreated(AlertCreatedEvent event) {
        AlertResponse alert = event.getAlert();
        
        log.info("🎧 Processing alert event: {}", event.getSummary());
        
        try {
            // 1️⃣ 전체 브로드캐스트
            notificationService.broadcastAlert(alert);
            
            // 2️⃣ 심각도별 채널
            notificationService.sendBySeverity(alert);
            
            // 3️⃣ 설비별 알림
            notificationService.sendToFacilityUsers(event.getFacilityId(), alert);
            
            // 4️⃣ 대시보드 업데이트
            notificationService.updateDashboard(alert);
            
            // 5️⃣ 고심각도 알림 특별 처리
            if (event.isHighSeverity()) {
                handleHighSeverityAlert(event);
            }
            
            // 6️⃣ 최근 알림 특별 처리
            if (event.isRecent()) {
                handleRecentAlert(event);
            }
            
            log.info("✅ Alert event processing completed: {}", alert.getId());
            
        } catch (Exception e) {
            log.error("❌ Error processing alert event: {}", event.getSummary(), e);
        }
    }
    
    /**
     * 고심각도 알림 특별 처리
     */
    private void handleHighSeverityAlert(AlertCreatedEvent event) {
        AlertResponse alert = event.getAlert();
        
        log.warn("🚨 HIGH SEVERITY ALERT detected: {}", alert.getSummary());
        
        // 관리자들에게 개별 알림
        sendToManagers(alert);
        
        // 이메일 알림 (추후 구현)
        sendEmailToManagers(alert);
        
        // 긴급 SMS (매우 심각한 경우 - 추후 구현)
        if (isEmergencyLevel(alert)) {
            sendEmergencySms(alert);
        }
    }
    
    /**
     * 최근 알림 특별 처리
     */
    private void handleRecentAlert(AlertCreatedEvent event) {
        AlertResponse alert = event.getAlert();
        
        log.info("⏰ Recent alert detected: {}", alert.getSummary());
        
        // 실시간 대시보드 우선 표시
        notificationService.updateDashboard(alert);
        
        // 현장 작업자들에게 즉시 알림
        sendToOperators(alert);
    }
    
    /**
     * 관리자들에게 개별 알림 전송
     */
    private void sendToManagers(AlertResponse alert) {
        try {
            List<String> managerIds = userService.getManagerUserIds();
            
            for (String managerId : managerIds) {
                notificationService.sendPersonalAlert(managerId, alert);
            }
            
            log.info("👔 Sent personal alerts to {} managers", managerIds.size());
            
        } catch (Exception e) {
            log.error("❌ Error sending alerts to managers", e);
        }
    }
    
    /**
     * 작업자들에게 알림 전송
     */
    private void sendToOperators(AlertResponse alert) {
        try {
            List<String> operatorIds = userService.getOperatorUserIds();
            
            for (String operatorId : operatorIds) {
                notificationService.sendPersonalAlert(operatorId, alert);
            }
            
            log.info("⚙️ Sent personal alerts to {} operators", operatorIds.size());
            
        } catch (Exception e) {
            log.error("❌ Error sending alerts to operators", e);
        }
    }
    
    /**
     * 관리자들에게 이메일 알림
     */
    private void sendEmailToManagers(AlertResponse alert) {
        try {
            List<String> managerEmails = userService.getManagerEmails();
            
            for (String email : managerEmails) {
                notificationService.sendEmailAlert(alert, email);
            }
            
            log.info("📧 Sent email alerts to {} managers", managerEmails.size());
            
        } catch (Exception e) {
            log.error("❌ Error sending email alerts to managers", e);
        }
    }
    
    /**
     * 긴급 SMS 전송
     */
    private void sendEmergencySms(AlertResponse alert) {
        try {
            List<String> emergencyPhones = userService.getEmergencyPhoneNumbers();
            
            for (String phone : emergencyPhones) {
                notificationService.sendSmsAlert(alert, phone);
            }
            
            log.warn("📱 Sent emergency SMS to {} numbers", emergencyPhones.size());
            
        } catch (Exception e) {
            log.error("❌ Error sending emergency SMS", e);
        }
    }
    
    /**
     * 긴급 레벨 판단
     */
    private boolean isEmergencyLevel(AlertResponse alert) {
        // 임계값을 크게 벗어난 경우
        Double value = alert.getValue();
        Double thresholdMin = alert.getThresholdMin();
        Double thresholdMax = alert.getThresholdMax();
        
        if (thresholdMin == null || thresholdMax == null) {
            return false;
        }
        
        double range = thresholdMax - thresholdMin;
        
        // 임계값 범위의 100% 이상 벗어나면 긴급
        if (value < thresholdMin) {
            return Math.abs(thresholdMin - value) > range;
        } else if (value > thresholdMax) {
            return Math.abs(value - thresholdMax) > range;
        }
        
        return false;
    }
    
    /**
     * 알림 처리 통계 (비동기)
     */
    @EventListener
    @Async("taskExecutor") // 🚀 일반 작업용 스레드 풀 사용
    public void updateNotificationStats(AlertCreatedEvent event) {
        // TODO: 알림 통계 업데이트
        notificationService.logNotificationStats();
    }
}
