package com.smartfactory.smartmes_insight.domain.sensor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {
    List<Sensor> findByFacilityId(Long facilityId);
    
    /**
     * 활성화된 센서 ID 목록 조회 (스케줄러용)
     * 임계값이 설정되고 설비가 활성화된 센서만 조회
     */
    @Query("SELECT s.id FROM Sensor s JOIN s.facility f " +
           "WHERE f.status = '가동중' AND s.thresholdMin IS NOT NULL AND s.thresholdMax IS NOT NULL")
    List<Long> findAllActiveSensorIds();
}