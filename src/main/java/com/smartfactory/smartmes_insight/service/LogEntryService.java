package com.smartfactory.smartmes_insight.service;

import com.smartfactory.smartmes_insight.domain.log.LogEntry;
import com.smartfactory.smartmes_insight.domain.log.LogEntryRepository;
import com.smartfactory.smartmes_insight.domain.user.User;
import com.smartfactory.smartmes_insight.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LogEntryService {

    private final LogEntryRepository logEntryRepository;
    private final UserRepository userRepository;

    // 사용자 행동 기록 저장
    public LogEntry save(LogEntry logEntry) {
        return logEntryRepository.save(logEntry);
    }

    // 사용자 행동 기록 생성 및 저장
    public LogEntry recordUserAction(Long userId, String action, String targetTable, Long targetId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LogEntry logEntry = LogEntry.builder()
                .user(user)
                .action(action)
                .targetTable(targetTable)
                .targetId(targetId)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        return logEntryRepository.save(logEntry);
    }

    // 로그 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<LogEntry> findAll(Pageable pageable) {
        return logEntryRepository.findAllByOrderByTimestampDesc(pageable);
    }

    // 전체 로그 조회
    @Transactional(readOnly = true)
    public List<LogEntry> findAll() {
        return logEntryRepository.findAllByOrderByTimestampDesc();
    }

    // 특정 사용자의 로그 조회
    @Transactional(readOnly = true)
    public List<LogEntry> findByUserId(Long userId) {
        return logEntryRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    // 특정 액션의 로그 조회
    @Transactional(readOnly = true)
    public List<LogEntry> findByAction(String action) {
        return logEntryRepository.findByActionOrderByTimestampDesc(action);
    }

    // 특정 테이블의 로그 조회
    @Transactional(readOnly = true)
    public List<LogEntry> findByTargetTable(String targetTable) {
        return logEntryRepository.findByTargetTableOrderByTimestampDesc(targetTable);
    }

    // 특정 기간의 로그 조회
    @Transactional(readOnly = true)
    public List<LogEntry> findByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return logEntryRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);
    }

    // 특정 사용자와 기간의 로그 조회
    @Transactional(readOnly = true)
    public List<LogEntry> findByUserAndPeriod(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return logEntryRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(userId, startDate, endDate);
    }

    // 로그 단건 조회
    @Transactional(readOnly = true)
    public Optional<LogEntry> findById(Long id) {
        return logEntryRepository.findById(id);
    }

    // 기간별 시스템 통계 제공
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<LogEntry> logs = logEntryRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);

        // 액션별 통계
        Map<String, Long> actionStats = logs.stream()
                .collect(Collectors.groupingBy(LogEntry::getAction, Collectors.counting()));

        // 사용자별 활동 통계
        Map<String, Long> userStats = logs.stream()
                .collect(Collectors.groupingBy(
                    log -> log.getUser().getUsername(), 
                    Collectors.counting()
                ));

        // 테이블별 활동 통계
        Map<String, Long> tableStats = logs.stream()
                .collect(Collectors.groupingBy(LogEntry::getTargetTable, Collectors.counting()));

        return Map.of(
                "totalLogs", logs.size(),
                "actionStatistics", actionStats,
                "userStatistics", userStats,
                "tableStatistics", tableStats,
                "period", Map.of(
                    "startDate", startDate,
                    "endDate", endDate
                )
        );
    }

    // 일별 활동 통계
    @Transactional(readOnly = true)
    public Map<String, Object> getDailyStatistics(LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        
        return getSystemStatistics(startOfDay, endOfDay);
    }

    // 월별 활동 통계
    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyStatistics(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        
        return getSystemStatistics(startOfMonth, endOfMonth);
    }

    // 최근 활동 로그 조회 (개수 제한)
    @Transactional(readOnly = true)
    public List<LogEntry> findRecentLogs(int limit) {
        return logEntryRepository.findTop50ByOrderByTimestampDesc();
    }

    // 특정 대상의 로그 조회 (테이블명 + ID)
    @Transactional(readOnly = true)
    public List<LogEntry> findByTarget(String targetTable, Long targetId) {
        return logEntryRepository.findByTargetTableAndTargetIdOrderByTimestampDesc(targetTable, targetId);
    }

    // 로그 삭제 (오래된 로그 정리용)
    public void deleteOldLogs(LocalDateTime beforeDate) {
        logEntryRepository.deleteByTimestampBefore(beforeDate);
    }

    // 특정 사용자별 최근 활동 요약
    @Transactional(readOnly = true)
    public Map<String, Object> getUserActivitySummary(Long userId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<LogEntry> userLogs = logEntryRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
                userId, startDate, endDate);

        Map<String, Long> actionCounts = userLogs.stream()
                .collect(Collectors.groupingBy(LogEntry::getAction, Collectors.counting()));

        return Map.of(
                "userId", userId,
                "period", days + " days",
                "totalActions", userLogs.size(),
                "actionBreakdown", actionCounts,
                "lastActivity", userLogs.isEmpty() ? null : userLogs.get(0).getTimestamp()
        );
    }
}
