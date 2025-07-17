package com.smartfactory.smartmes_insight.service;

import com.smartfactory.smartmes_insight.domain.facility.Facility;
import com.smartfactory.smartmes_insight.domain.facility.FacilityRepository;
import com.smartfactory.smartmes_insight.dto.facility.FacilityCreateRequest;
import com.smartfactory.smartmes_insight.dto.facility.FacilityResponse;
import com.smartfactory.smartmes_insight.dto.facility.FacilityUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FacilityService {

    private final FacilityRepository facilityRepository;

    // 설비 등록
    public Facility save(Facility facility) {
        return facilityRepository.save(facility);
    }

    // 설비 전체 조회
    @Transactional(readOnly = true)
    public List<Facility> findAll() {
        return facilityRepository.findAll();
    }

    // 설비 단건 조회
    @Transactional(readOnly = true)
    public Optional<Facility> findById(Long id) {
        return facilityRepository.findById(id);
    }

    // 설비 정보 수정
    public void updateFacility(Long id, Facility updatedFacility) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("설비를 찾을 수 없습니다."));

        facility.updateInfo(updatedFacility); // 엔티티 내부에서 수정 메서드 제공
    }

    // 설비 삭제
    public void deleteById(Long id) {
        facilityRepository.deleteById(id);
    }
    
    // 🔍 설비 엔티티 직접 조회 (서비스간 통신용)
    @Transactional(readOnly = true)
    public Facility findEntityById(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("설비를 찾을 수 없습니다."));
    }

    // ========================= Controller용 메서드들 =========================

    // 설비 생성 (DTO 기반)
    public FacilityResponse createFacility(FacilityCreateRequest request) {
        // TODO: 실제 구현 필요
        return null;
    }

    // 전체 설비 조회 (DTO 기반)
    @Transactional(readOnly = true)
    public List<FacilityResponse> getAllFacilities() {
        // TODO: 실제 구현 필요
        return List.of();
    }

    // 설비 상세 조회 (DTO 기반)
    @Transactional(readOnly = true)
    public FacilityResponse getFacilityById(Long id) {
        // TODO: 실제 구현 필요
        return null;
    }

    // 설비명으로 조회
    @Transactional(readOnly = true)
    public FacilityResponse getFacilityByName(String name) {
        // TODO: 실제 구현 필요
        return null;
    }

    // 상태별 설비 조회
    @Transactional(readOnly = true)
    public List<FacilityResponse> getFacilitiesByStatus(String status) {
        // TODO: 실제 구현 필요
        return List.of();
    }

    // 가동 중인 설비 조회
    @Transactional(readOnly = true)
    public List<FacilityResponse> getActiveFacilities() {
        // TODO: 실제 구현 필요
        return List.of();
    }

    // 설비 정보 수정 (DTO 기반)
    public FacilityResponse updateFacility(Long id, FacilityUpdateRequest request) {
        // TODO: 실제 구현 필요
        return null;
    }

    // 설비 삭제 (DTO 기반)
    public void deleteFacility(Long id) {
        deleteById(id);
    }

    // 설비 상태 변경
    public FacilityResponse changeFacilityStatus(Long id, String status) {
        // TODO: 실제 구현 필요
        return null;
    }

    // 설비 가동 시작
    public FacilityResponse startFacility(Long id) {
        // TODO: 실제 구현 필요
        return null;
    }

    // 설비 가동 정지
    public FacilityResponse stopFacility(Long id) {
        // TODO: 실제 구현 필요
        return null;
    }

}
