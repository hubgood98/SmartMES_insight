package com.smartfactory.smartmes_insight.controller;

import com.smartfactory.smartmes_insight.common.ApiResponse;
import com.smartfactory.smartmes_insight.domain.log.LogEntry;
import com.smartfactory.smartmes_insight.service.LogEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "📋 로그 관리", description = "사용자 활동 로그 및 감사 추적 API")
public class LogEntryController {

    private final LogEntryService logEntryService;

    @Operation(summary = "사용자 활동 기록", description = "사용자의 활동을 수동으로 기록합니다. (ADMIN, MANAGER 권한 필요)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<LogEntry>> recordUserAction(
            @RequestParam @Parameter(description = "사용자 ID") Long userId,
            @RequestParam @Parameter(description = "액션 타입") String action,
            @RequestParam @Parameter(description = "대상 테이블") String targetTable,
            @RequestParam @Parameter(description = "대상 ID") Long targetId,
            @RequestParam @Parameter(description = "메시지") String message) {
        LogEntry logEntry = logEntryService.recordUserAction(userId, action, targetTable, targetId, message);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(logEntry, "사용자 활동이 성공적으로 기록되었습니다."));
    }

    @Operation(summary = "전체 로그 조회 (페이징)", description = "모든 로그를 페이징하여 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/paged")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Page<LogEntry>>> getAllLogsPaged(
            @RequestParam(defaultValue = "0") @Parameter(description = "페이지 번호") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "페이지 크기") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LogEntry> logs = logEntryService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(logs, "로그 목록 조회 성공"));
    }

    @Operation(summary = "전체 로그 조회", description = "모든 로그를 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<LogEntry>>> getAllLogs() {
        List<LogEntry> logs = logEntryService.findAll();
        return ResponseEntity.ok(ApiResponse.success(logs, "로그 목록 조회 성공"));
    }

    @Operation(summary = "로그 상세 조회", description = "특정 로그의 상세 정보를 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<LogEntry>> getLogById(
            @PathVariable @Parameter(description = "로그 ID") Long id) {
        Optional<LogEntry> log = logEntryService.findById(id);
        if (log.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(log.get(), "로그 조회 성공"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, "로그를 찾을 수 없습니다."));
    }

    @Operation(summary = "사용자별 로그 조회", description = "특정 사용자의 로그를 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<LogEntry>>> getLogsByUser(
            @PathVariable @Parameter(description = "사용자 ID") Long userId) {
        List<LogEntry> logs = logEntryService.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(logs, "사용자별 로그 조회 성공"));
    }

    @Operation(summary = "액션별 로그 조회", description = "특정 액션의 로그를 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<LogEntry>>> getLogsByAction(
            @PathVariable @Parameter(description = "액션 타입") String action) {
        List<LogEntry> logs = logEntryService.findByAction(action);
        return ResponseEntity.ok(ApiResponse.success(logs, "액션별 로그 조회 성공"));
    }

    @Operation(summary = "테이블별 로그 조회", description = "특정 테이블의 로그를 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/table/{targetTable}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<LogEntry>>> getLogsByTable(
            @PathVariable @Parameter(description = "대상 테이블") String targetTable) {
        List<LogEntry> logs = logEntryService.findByTargetTable(targetTable);
        return ResponseEntity.ok(ApiResponse.success(logs, "테이블별 로그 조회 성공"));
    }

    @Operation(summary = "기간별 로그 조회", description = "특정 기간의 로그를 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/period")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<LogEntry>>> getLogsByPeriod(
            @RequestParam @Parameter(description = "시작 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @Parameter(description = "종료 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<LogEntry> logs = logEntryService.findByPeriod(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(logs, "기간별 로그 조회 성공"));
    }

    @Operation(summary = "사용자별 기간 로그 조회", description = "특정 사용자의 특정 기간 로그를 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/user/{userId}/period")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<LogEntry>>> getLogsByUserAndPeriod(
            @PathVariable @Parameter(description = "사용자 ID") Long userId,
            @RequestParam @Parameter(description = "시작 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @Parameter(description = "종료 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<LogEntry> logs = logEntryService.findByUserAndPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(logs, "사용자별 기간 로그 조회 성공"));
    }

    @Operation(summary = "특정 대상 로그 조회", description = "특정 테이블의 특정 대상 로그를 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/target/{targetTable}/{targetId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<LogEntry>>> getLogsByTarget(
            @PathVariable @Parameter(description = "대상 테이블") String targetTable,
            @PathVariable @Parameter(description = "대상 ID") Long targetId) {
        List<LogEntry> logs = logEntryService.findByTarget(targetTable, targetId);
        return ResponseEntity.ok(ApiResponse.success(logs, "대상별 로그 조회 성공"));
    }

    @Operation(summary = "최근 로그 조회", description = "최근 로그를 제한된 개수만큼 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<LogEntry>>> getRecentLogs(
            @RequestParam(defaultValue = "50") @Parameter(description = "조회할 개수") int limit) {
        List<LogEntry> logs = logEntryService.findRecentLogs(limit);
        return ResponseEntity.ok(ApiResponse.success(logs, "최근 로그 조회 성공"));
    }

    @Operation(summary = "시스템 통계 조회", description = "특정 기간의 시스템 활동 통계를 조회합니다. (ADMIN 권한 필요)")
    @GetMapping("/statistics/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStatistics(
            @RequestParam @Parameter(description = "시작 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @Parameter(description = "종료 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Map<String, Object> statistics = logEntryService.getSystemStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics, "시스템 통계 조회 성공"));
    }

    @Operation(summary = "일일 통계 조회", description = "특정 날짜의 일일 활동 통계를 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/statistics/daily")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDailyStatistics(
            @RequestParam @Parameter(description = "조회 날짜") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        Map<String, Object> statistics = logEntryService.getDailyStatistics(date);
        return ResponseEntity.ok(ApiResponse.success(statistics, "일일 통계 조회 성공"));
    }

    @Operation(summary = "월별 통계 조회", description = "특정 월의 활동 통계를 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/statistics/monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyStatistics(
            @RequestParam @Parameter(description = "연도") int year,
            @RequestParam @Parameter(description = "월") int month) {
        Map<String, Object> statistics = logEntryService.getMonthlyStatistics(year, month);
        return ResponseEntity.ok(ApiResponse.success(statistics, "월별 통계 조회 성공"));
    }

    @Operation(summary = "사용자 활동 요약", description = "특정 사용자의 최근 활동 요약을 조회합니다. (ADMIN, MANAGER 권한 필요)")
    @GetMapping("/user/{userId}/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserActivitySummary(
            @PathVariable @Parameter(description = "사용자 ID") Long userId,
            @RequestParam(defaultValue = "7") @Parameter(description = "조회 기간 (일)") int days) {
        Map<String, Object> summary = logEntryService.getUserActivitySummary(userId, days);
        return ResponseEntity.ok(ApiResponse.success(summary, "사용자 활동 요약 조회 성공"));
    }

    @Operation(summary = "오래된 로그 삭제", description = "지정된 날짜 이전의 로그를 삭제합니다. (ADMIN 권한 필요)")
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteOldLogs(
            @RequestParam @Parameter(description = "삭제 기준 일시") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate) {
        logEntryService.deleteOldLogs(beforeDate);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent("오래된 로그가 성공적으로 삭제되었습니다."));
    }
}
