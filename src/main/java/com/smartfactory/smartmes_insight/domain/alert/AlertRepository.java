package com.smartfactory.smartmes_insight.domain.alert;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    // 생성일시 내림차순으로 전체 알림 조회
    List<Alert> findAllByOrderByCreatedAtDesc();
    
    // 특정 센서의 알림을 생성일시 내림차순으로 조회
    List<Alert> findBySensorIdOrderByCreatedAtDesc(Long sensorId);
    
    // 특정 기간의 알림을 생성일시 내림차순으로 조회
    List<Alert> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    // 최근 알림 n개 조회 (생성일시 내림차순) - Pageable 사용
    @Query("SELECT a FROM Alert a ORDER BY a.createdAt DESC")
    List<Alert> findRecentAlerts(Pageable pageable);
    
    // 특정 센서의 알림 전체 삭제
    void deleteBySensorId(Long sensorId);
    
    // 특정 날짜 이전의 오래된 알림 삭제
    @Modifying
    @Query("DELETE FROM Alert a WHERE a.createdAt < :cutoffDate")
    void deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // 특정 센서의 알림 개수 조회
    long countBySensorId(Long sensorId);
    
    // 특정 기간의 알림 개수 조회
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // 특정 센서의 최근 알림 조회 (개수 제한)
    List<Alert> findTopBySensorIdOrderByCreatedAtDesc(Long sensorId);
}
