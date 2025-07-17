package com.smartfactory.smartmes_insight.controller;

import com.smartfactory.smartmes_insight.common.ApiResponse;
import com.smartfactory.smartmes_insight.domain.sensor.SensorLog;
import com.smartfactory.smartmes_insight.service.SensorLogService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/sensor-logs")
@RequiredArgsConstructor
@Tag(name = "📊 센서 로그 관리", description = "센서 데이터 수집 및 분석 API")
public class SensorLogController {

    private final SensorLogService sensorLogService;

    @Operation(summary = "센서 데이터 수동 저장", description = "센서 데이터를 수동으로 저장합니다. (ADMIN, MANAGER 권한 필요)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<SensorLog>> saveSensorData(
            @RequestParam @Parameter(description = "센서 ID") Long sensorId,
            @RequestParam @Parameter(description = "센서 값") Double value) {
        SensorLog sensorLog = sensorLogService.saveSensorData(sensorId, value);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(sensorLog, "센서 데이터가 성공적으로 저장되었습니다."));
    }

    @Operation(summary = "센서별 기간 로그 조회", description = "특정 센서의 특정 기간 로그를 조회합니다.")
    @GetMapping("/sensor/{sensorId}/period")
    public ResponseEntity<ApiResponse<List<SensorLog>>> getSensorLogsByPeriod(
            @PathVariable @Parameter(description = "센서 ID") Long sensorId,
            @RequestParam @Parameter(description = "시작 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @Parameter(description = "종료 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<SensorLog> logs = sensorLogService.findByPeriod(sensorId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(logs, "센서 기간별 로그 조회 성공"));
    }

    @Operation(summary = "센서별 최근 로그 조회", description = "특정 센서의 최근 로그를 조회합니다.")
    @GetMapping("/sensor/{sensorId}/recent")
    public ResponseEntity<ApiResponse<List<SensorLog>>> getRecentSensorLogs(
            @PathVariable @Parameter(description = "센서 ID") Long sensorId,
            @RequestParam(defaultValue = "10") @Parameter(description = "조회할 개수") int limit) {
        List<SensorLog> logs = sensorLogService.findRecentLogsBySensorId(sensorId, limit);
        return ResponseEntity.ok(ApiResponse.success(logs, "센서 최근 로그 조회 성공"));
    }

    @Operation(summary = "센서 이상 패턴 감지", description = "특정 센서의 특정 기간 동안 이상 패턴을 감지합니다.")
    @GetMapping("/sensor/{sensorId}/anomalies")
    public ResponseEntity<ApiResponse<List<SensorLog>>> detectAnomalies(
            @PathVariable @Parameter(description = "센서 ID") Long sensorId,
            @RequestParam @Parameter(description = "시작 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @Parameter(description = "종료 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<SensorLog> anomalies = sensorLogService.detectAnomalies(sensorId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(anomalies, "센서 이상 패턴 감지 성공"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @Operation(summary = "센서 통계 조회", description = "특정 센서의 특정 기간 통계를 조회합니다.")
    @GetMapping("/sensor/{sensorId}/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSensorStatistics(
            @PathVariable @Parameter(description = "센서 ID") Long sensorId,
            @RequestParam @Parameter(description = "시작 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @Parameter(description = "종료 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Map<String, Object> statistics = sensorLogService.getStatistics(sensorId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics, "센서 통계 조회 성공"));
    }

    @Operation(summary = "전체 센서 최신 데이터 조회", description = "모든 센서의 최신 데이터를 조회합니다.")
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<SensorLog>>> getLatestDataForAllSensors() {
        List<SensorLog> latestLogs = sensorLogService.findLatestDataForAllSensors();
        return ResponseEntity.ok(ApiResponse.success(latestLogs, "전체 센서 최신 데이터 조회 성공"));
    }
}
