package com.smartfactory.smartmes_insight.controller;

import com.smartfactory.smartmes_insight.common.ApiResponse;
import com.smartfactory.smartmes_insight.dto.AlertResponse;
import com.smartfactory.smartmes_insight.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "🚨 알림 관리", description = "센서 이상 감지 및 알림 관리 API")
public class AlertController {

    private final AlertService alertService;

    @Operation(summary = "수동 알림 생성", description = "수동으로 알림을 생성합니다. (ADMIN, MANAGER 권한 필요)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<AlertResponse>> createAlert(
            @RequestParam @Parameter(description = "센서 ID") Long sensorId,
            @RequestParam @Parameter(description = "센서 값") Double value,
            @RequestParam @Parameter(description = "알림 메시지") String message) {
        AlertResponse alert = alertService.createAlert(sensorId, value, message);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(alert, "알림이 성공적으로 생성되었습니다."));
    }

    @Operation(summary = "전체 알림 조회", description = "모든 알림 목록을 조회합니다. (최신순)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAllAlerts() {
        List<AlertResponse> alerts = alertService.findAll();
        return ResponseEntity.ok(ApiResponse.success(alerts, "알림 목록 조회 성공"));
    }

    @Operation(summary = "알림 상세 조회", description = "특정 알림의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertResponse>> getAlertById(
            @PathVariable @Parameter(description = "알림 ID") Long id) {
        Optional<AlertResponse> alert = alertService.findById(id);
        if (alert.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(alert.get(), "알림 조회 성공"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, "알림을 찾을 수 없습니다."));
    }

    @Operation(summary = "센서별 알림 조회", description = "특정 센서의 알림 목록을 조회합니다.")
    @GetMapping("/sensor/{sensorId}")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlertsBySensor(
            @PathVariable @Parameter(description = "센서 ID") Long sensorId) {
        List<AlertResponse> alerts = alertService.findBySensorId(sensorId);
        return ResponseEntity.ok(ApiResponse.success(alerts, "센서별 알림 조회 성공"));
    }

    @Operation(summary = "기간별 알림 조회", description = "특정 기간의 알림 목록을 조회합니다.")
    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlertsByPeriod(
            @RequestParam @Parameter(description = "시작 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @Parameter(description = "종료 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AlertResponse> alerts = alertService.findByPeriod(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(alerts, "기간별 알림 조회 성공"));
    }

    @Operation(summary = "최근 알림 조회", description = "최근 알림을 제한된 개수만큼 조회합니다.")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getRecentAlerts(
            @RequestParam(defaultValue = "10") @Parameter(description = "조회할 개수") int limit) {
        List<AlertResponse> alerts = alertService.findRecentAlerts(limit);
        return ResponseEntity.ok(ApiResponse.success(alerts, "최근 알림 조회 성공"));
    }

    @Operation(summary = "심각도별 알림 조회", description = "특정 심각도의 알림 목록을 조회합니다.")
    @GetMapping("/severity/{severity}")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlertsBySeverity(
            @PathVariable @Parameter(description = "심각도 (HIGH, MEDIUM, LOW)") String severity) {
        List<AlertResponse> alerts = alertService.findBySeverity(severity);
        return ResponseEntity.ok(ApiResponse.success(alerts, "심각도별 알림 조회 성공"));
    }

    @Operation(summary = "최근 30분 내 알림 조회", description = "최근 30분 이내에 발생한 알림만 조회합니다.")
    @GetMapping("/recent/active")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getRecentActiveAlerts() {
        List<AlertResponse> alerts = alertService.findRecentAlertsOnly();
        return ResponseEntity.ok(ApiResponse.success(alerts, "최근 활성 알림 조회 성공"));
    }

    @Operation(summary = "설비별 알림 조회", description = "특정 설비의 알림 목록을 조회합니다.")
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlertsByFacility(
            @PathVariable @Parameter(description = "설비 ID") Long facilityId) {
        List<AlertResponse> alerts = alertService.findByFacilityId(facilityId);
        return ResponseEntity.ok(ApiResponse.success(alerts, "설비별 알림 조회 성공"));
    }

    @Operation(summary = "알림 요약 정보 조회", description = "최근 알림들의 요약 정보를 조회합니다.")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<String>>> getAlertSummaries(
            @RequestParam(defaultValue = "5") @Parameter(description = "조회할 개수") int limit) {
        List<String> summaries = alertService.getAlertSummaries(limit);
        return ResponseEntity.ok(ApiResponse.success(summaries, "알림 요약 정보 조회 성공"));
    }

    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다. (ADMIN 권한 필요)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAlert(
            @PathVariable @Parameter(description = "알림 ID") Long id) {
        alertService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent("알림이 성공적으로 삭제되었습니다."));
    }

    @Operation(summary = "센서별 알림 전체 삭제", description = "특정 센서의 모든 알림을 삭제합니다. (ADMIN 권한 필요)")
    @DeleteMapping("/sensor/{sensorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAlertsBySensor(
            @PathVariable @Parameter(description = "센서 ID") Long sensorId) {
        alertService.deleteBySensorId(sensorId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent("센서의 모든 알림이 성공적으로 삭제되었습니다."));
    }

    @Operation(summary = "오래된 알림 정리", description = "지정된 일수보다 오래된 알림들을 삭제합니다. (ADMIN 권한 필요)")
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cleanupOldAlerts(
            @RequestParam(defaultValue = "30") @Parameter(description = "보관 기간 (일)") int daysToKeep) {
        alertService.deleteOldAlerts(daysToKeep);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent("오래된 알림이 성공적으로 정리되었습니다."));
    }

    @Operation(summary = "센서 임계값 자동 체크", description = "센서 값에 대한 임계값을 체크하고 필요시 알림을 생성합니다.")
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<Optional<AlertResponse>>> checkAndCreateAlert(
            @RequestParam @Parameter(description = "센서 ID") Long sensorId,
            @RequestParam @Parameter(description = "센서 값") Double value) {
        Optional<AlertResponse> alert = alertService.checkAndCreateAlert(sensorId, value);
        if (alert.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(alert, "임계값 초과로 알림이 생성되었습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success(alert, "센서 값이 정상 범위 내에 있습니다."));
    }
}
