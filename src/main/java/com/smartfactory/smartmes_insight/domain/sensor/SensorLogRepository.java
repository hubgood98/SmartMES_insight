package com.smartfactory.smartmes_insight.domain.sensor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {
    List<SensorLog> findBySensorIdAndCollectedAtBetween(Long sensorId, LocalDateTime start, LocalDateTime end);
}