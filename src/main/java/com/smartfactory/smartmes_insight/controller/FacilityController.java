package com.smartfactory.smartmes_insight.controller;

import com.smartfactory.smartmes_insight.common.ApiResponse;
import com.smartfactory.smartmes_insight.dto.facility.*;
import com.smartfactory.smartmes_insight.service.FacilityService;
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
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
@Tag(name = "🏭 설비 관리", description = "생산 설비 관리 및 모니터링 API")
public class FacilityController {

    private final FacilityService facilityService;

    @Operation(summary = "설비 등록", description = "새로운 생산 설비를 등록합니다. (ADMIN, MANAGER 권한 필요)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<FacilityResponse>> createFacility(
            @Valid @RequestBody @Parameter(description = "설비 생성 요청 데이터") FacilityCreateRequest request) {
        FacilityResponse facility = facilityService.createFacility(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(facility, "설비가 성공적으로 등록되었습니다."));
    }

    @Operation(summary = "전체 설비 조회", description = "모든 설비 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FacilityResponse>>> getAllFacilities() {
        List<FacilityResponse> facilities = facilityService.getAllFacilities();
        return ResponseEntity.ok(ApiResponse.success(facilities, "설비 목록 조회 성공"));
    }

    @Operation(summary = "설비 상세 조회", description = "특정 설비의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacilityResponse>> getFacilityById(
            @PathVariable @Parameter(description = "설비 ID") Long id) {
        FacilityResponse facility = facilityService.getFacilityById(id);
        return ResponseEntity.ok(ApiResponse.success(facility, "설비 조회 성공"));
    }

    @Operation(summary = "설비 정보 수정", description = "설비 정보를 수정합니다. (ADMIN, MANAGER 권한 필요)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<FacilityResponse>> updateFacility(
            @PathVariable @Parameter(description = "설비 ID") Long id,
            @Valid @RequestBody @Parameter(description = "설비 수정 요청 데이터") FacilityUpdateRequest request) {
        FacilityResponse facility = facilityService.updateFacility(id, request);
        return ResponseEntity.ok(ApiResponse.success(facility, "설비 정보가 성공적으로 수정되었습니다."));
    }

    @Operation(summary = "설비 삭제", description = "설비를 삭제합니다. (ADMIN 권한 필요)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFacility(
            @PathVariable @Parameter(description = "설비 ID") Long id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent("설비가 성공적으로 삭제되었습니다."));
    }

    @Operation(summary = "설비명으로 조회", description = "설비명으로 설비를 조회합니다.")
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<FacilityResponse>> getFacilityByName(
            @PathVariable @Parameter(description = "설비명") String name) {
        FacilityResponse facility = facilityService.getFacilityByName(name);
        return ResponseEntity.ok(ApiResponse.success(facility, "설비 조회 성공"));
    }

    @Operation(summary = "상태별 설비 조회", description = "특정 상태의 설비 목록을 조회합니다.")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<FacilityResponse>>> getFacilitiesByStatus(
            @PathVariable @Parameter(description = "설비 상태 (가동중, 정지중, 점검중)") String status) {
        List<FacilityResponse> facilities = facilityService.getFacilitiesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(facilities, "상태별 설비 조회 성공"));
    }

    @Operation(summary = "가동 중인 설비 조회", description = "현재 가동 중인 설비 목록을 조회합니다.")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<FacilityResponse>>> getActiveFacilities() {
        List<FacilityResponse> facilities = facilityService.getActiveFacilities();
        return ResponseEntity.ok(ApiResponse.success(facilities, "가동 중인 설비 조회 성공"));
    }

    @Operation(summary = "설비 상태 변경", description = "설비의 상태를 변경합니다. (ADMIN, MANAGER 권한 필요)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<FacilityResponse>> changeFacilityStatus(
            @PathVariable @Parameter(description = "설비 ID") Long id,
            @RequestParam @Parameter(description = "새로운 상태") String status) {
        FacilityResponse facility = facilityService.changeFacilityStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(facility, "설비 상태가 성공적으로 변경되었습니다."));
    }

    @Operation(summary = "설비 가동 시작", description = "설비를 가동 상태로 변경합니다. (MANAGER, OPERATOR 권한 필요)")
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('MANAGER') or hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<FacilityResponse>> startFacility(
            @PathVariable @Parameter(description = "설비 ID") Long id) {
        FacilityResponse facility = facilityService.startFacility(id);
        return ResponseEntity.ok(ApiResponse.success(facility, "설비가 성공적으로 가동되었습니다."));
    }

    @Operation(summary = "설비 가동 정지", description = "설비를 정지 상태로 변경합니다. (MANAGER, OPERATOR 권한 필요)")
    @PostMapping("/{id}/stop")
    @PreAuthorize("hasRole('MANAGER') or hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<FacilityResponse>> stopFacility(
            @PathVariable @Parameter(description = "설비 ID") Long id) {
        FacilityResponse facility = facilityService.stopFacility(id);
        return ResponseEntity.ok(ApiResponse.success(facility, "설비가 성공적으로 정지되었습니다."));
    }
}
