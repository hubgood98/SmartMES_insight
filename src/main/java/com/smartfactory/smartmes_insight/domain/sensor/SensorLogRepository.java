package com.smartfactory.smartmes_insight.domain.sensor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {
    List<SensorLog> findBySensorIdAndCollectedAtBetween(Long sensorId, LocalDateTime start, LocalDateTime end);
    
    List<SensorLog> findTop10BySensorIdOrderByCollectedAtDesc(Long sensorId);
    
    @Query("SELECT sl FROM SensorLog sl WHERE sl.sensor.id = :sensorId " +
           "AND sl.collectedAt BETWEEN :startDate AND :endDate " +
           "AND (sl.value < :thresholdMin OR sl.value > :thresholdMax)")
    List<SensorLog> findBySensorIdAndCollectedAtBetweenAndValueNotBetween(
            Long sensorId, LocalDateTime startDate, LocalDateTime endDate, 
            Double thresholdMin, Double thresholdMax);
    
    @Query("SELECT sl FROM SensorLog sl WHERE sl.id IN " +
           "(SELECT MAX(sl2.id) FROM SensorLog sl2 GROUP BY sl2.sensor.id)")
    List<SensorLog> findLatestDataForAllSensors();
}