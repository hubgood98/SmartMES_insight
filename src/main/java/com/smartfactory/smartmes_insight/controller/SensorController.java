package com.smartfactory.smartmes_insight.controller;

import com.smartfactory.smartmes_insight.common.ApiResponse;
import com.smartfactory.smartmes_insight.dto.sensor.SensorCreateRequest;
import com.smartfactory.smartmes_insight.dto.sensor.SensorResponse;
import com.smartfactory.smartmes_insight.dto.sensor.SensorUpdateRequest;
import com.smartfactory.smartmes_insight.dto.sensor.SensorSettingsRequest;
import com.smartfactory.smartmes_insight.service.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
@Tag(name = "📊 센서 관리", description = "센서 등록 및 임계값 설정 API")
public class SensorController {

    private final SensorService sensorService;

    @Operation(summary = "센서 등록", description = "새로운 센서를 등록합니다. (ADMIN, MANAGER 권한 필요)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<SensorResponse>> createSensor(
            @Valid @RequestBody @Parameter(description = "센서 생성 요청 데이터") SensorCreateRequest request) {
        SensorResponse sensor = sensorService.createSensor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(sensor, "센서가 성공적으로 등록되었습니다."));
    }

    @Operation(summary = "전체 센서 조회", description = "모든 센서 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SensorResponse>>> getAllSensors() {
        List<SensorResponse> sensors = sensorService.getAllSensors();
        return ResponseEntity.ok(ApiResponse.success(sensors, "센서 목록 조회 성공"));
    }

    @Operation(summary = "센서 상세 조회", description = "특정 센서의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SensorResponse>> getSensorById(
            @PathVariable @Parameter(description = "센서 ID") Long id) {
        SensorResponse sensor = sensorService.getSensorById(id);
        return ResponseEntity.ok(ApiResponse.success(sensor, "센서 조회 성공"));
    }

    @Operation(summary = "센서 정보 수정", description = "센서 정보를 수정합니다. (ADMIN, MANAGER 권한 필요)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<SensorResponse>> updateSensor(
            @PathVariable @Parameter(description = "센서 ID") Long id,
            @Valid @RequestBody @Parameter(description = "센서 수정 요청 데이터") SensorUpdateRequest request) {
        SensorResponse sensor = sensorService.updateSensor(id, request);
        return ResponseEntity.ok(ApiResponse.success(sensor, "센서 정보가 성공적으로 수정되었습니다."));
    }

    @Operation(summary = "센서 삭제", description = "센서를 삭제합니다. (ADMIN 권한 필요)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSensor(
            @PathVariable @Parameter(description = "센서 ID") Long id) {
        sensorService.deleteSensor(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent("센서가 성공적으로 삭제되었습니다."));
    }

    @Operation(summary = "설비별 센서 조회", description = "특정 설비에 연결된 센서 목록을 조회합니다.")
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<ApiResponse<List<SensorResponse>>> getSensorsByFacility(
            @PathVariable @Parameter(description = "설비 ID") Long facilityId) {
        List<SensorResponse> sensors = sensorService.getSensorsByFacility(facilityId);
        return ResponseEntity.ok(ApiResponse.success(sensors, "설비별 센서 조회 성공"));
    }

    @Operation(summary = "센서 임계값 설정", description = "센서의 임계값을 설정합니다. (ADMIN, MANAGER 권한 필요)")
    @PatchMapping("/{id}/settings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<SensorResponse>> updateSensorSettings(
            @PathVariable @Parameter(description = "센서 ID") Long id,
            @Valid @RequestBody @Parameter(description = "센서 설정 요청 데이터") SensorSettingsRequest request) {
        SensorResponse sensor = sensorService.updateSensorSettings(id, request);
        return ResponseEntity.ok(ApiResponse.success(sensor, "센서 임계값이 성공적으로 설정되었습니다."));
    }

    @Operation(summary = "센서 값 임계값 검증", description = "센서 값이 임계값 범위 내에 있는지 검증합니다.")
    @GetMapping("/{id}/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateSensorValue(
            @PathVariable @Parameter(description = "센서 ID") Long id,
            @RequestParam @Parameter(description = "검증할 센서 값") Double value) {
        boolean isValid = sensorService.isValueWithinThreshold(id, value);
        String message = isValid ? "센서 값이 정상 범위 내에 있습니다." : "센서 값이 임계값을 벗어났습니다.";
        return ResponseEntity.ok(ApiResponse.success(isValid, message));
    }

    @Operation(summary = "센서 임계값 설정 여부 확인", description = "센서에 임계값이 설정되어 있는지 확인합니다.")
    @GetMapping("/{id}/threshold/status")
    public ResponseEntity<ApiResponse<Boolean>> checkThresholdStatus(
            @PathVariable @Parameter(description = "센서 ID") Long id) {
        boolean hasThresholds = sensorService.hasThresholds(id);
        String message = hasThresholds ? "임계값이 설정되어 있습니다." : "임계값이 설정되지 않았습니다.";
        return ResponseEntity.ok(ApiResponse.success(hasThresholds, message));
    }

    @Operation(summary = "활성 센서 ID 목록 조회", description = "현재 활성화된 센서 ID 목록을 조회합니다. (모니터링 시스템용)")
    @GetMapping("/active/ids")
    public ResponseEntity<ApiResponse<List<Long>>> getActiveSensorIds() {
        List<Long> activeSensorIds = sensorService.findActiveSensorIds();
        return ResponseEntity.ok(ApiResponse.success(activeSensorIds, "활성 센서 ID 목록 조회 성공"));
    }
}
