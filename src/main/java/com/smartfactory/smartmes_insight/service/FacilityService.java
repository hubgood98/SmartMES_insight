package com.smartfactory.smartmes_insight.service;

import com.smartfactory.smartmes_insight.domain.facility.Facility;
import com.smartfactory.smartmes_insight.domain.facility.FacilityRepository;
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
    public List<Facility> findAll() {
        return facilityRepository.findAll();
    }

    // 설비 단건 조회
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

}
