package com.smartfactory.smartmes_insight.domain.log;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    // 전체 로그 조회 (시간 역순)
    List<LogEntry> findAllByOrderByTimestampDesc();
    
    // 전체 로그 조회 (페이징, 시간 역순)
    Page<LogEntry> findAllByOrderByTimestampDesc(Pageable pageable);

    // 특정 사용자의 로그 조회
    List<LogEntry> findByUserIdOrderByTimestampDesc(Long userId);

    // 특정 액션의 로그 조회
    List<LogEntry> findByActionOrderByTimestampDesc(String action);

    // 특정 테이블의 로그 조회
    List<LogEntry> findByTargetTableOrderByTimestampDesc(String targetTable);

    // 특정 기간의 로그 조회
    List<LogEntry> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate);

    // 특정 사용자와 기간의 로그 조회
    List<LogEntry> findByUserIdAndTimestampBetweenOrderByTimestampDesc(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // 최근 활동 로그 조회 (개수 제한)
    List<LogEntry> findTop50ByOrderByTimestampDesc();

    // 특정 대상의 로그 조회 (테이블명 + ID)
    List<LogEntry> findByTargetTableAndTargetIdOrderByTimestampDesc(String targetTable, Long targetId);

    // 오래된 로그 삭제
    void deleteByTimestampBefore(LocalDateTime beforeDate);
}
