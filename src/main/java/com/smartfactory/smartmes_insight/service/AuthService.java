package com.smartfactory.smartmes_insight.service;

import com.smartfactory.smartmes_insight.domain.log.LogEntry;
import com.smartfactory.smartmes_insight.domain.log.LogEntryRepository;
import com.smartfactory.smartmes_insight.domain.user.Role;
import com.smartfactory.smartmes_insight.domain.user.User;
import com.smartfactory.smartmes_insight.domain.user.UserRepository;
import com.smartfactory.smartmes_insight.dto.auth.LoginRequest;
import com.smartfactory.smartmes_insight.dto.auth.LoginResponse;
import com.smartfactory.smartmes_insight.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🔐 SmartMES Insight 인증 서비스
 * 
 * 핵심 기능:
 * - JWT 기반 로그인/로그아웃
 * - 토큰 검증 및 갱신
 * - 사용자 인증 상태 관리
 * - 보안 로깅 및 감사
 * 
 * @author SmartMES Team
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final LogEntryRepository logEntryRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // 🚫 토큰 블랙리스트 (로그아웃된 토큰 관리)
    // 실제 운영환경에서는 Redis 사용 권장
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    
    // 📊 로그인 시도 추적 (보안 강화)
    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    /**
     * 🔑 로그인 - JWT 토큰 발급
     * 
     * @param loginRequest 로그인 요청 (username, password)
     * @return JWT 토큰 + 사용자 정보
     * @throws BadCredentialsException 인증 실패 시
     * @throws DisabledException 비활성 계정 시
     */
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("🔐 로그인 시도: username={}", loginRequest.getUsername());
        
        try {
            // 1️⃣ 로그인 시도 횟수 확인
            validateLoginAttempts(loginRequest.getUsername());
            
            // 2️⃣ 사용자 조회 및 검증
            User user = validateUser(loginRequest);
            
            // 3️⃣ 비밀번호 검증
            validatePassword(loginRequest.getPassword(), user.getPassword(), loginRequest.getUsername());
            
            // 4️⃣ 계정 상태 확인
            validateAccountStatus(user);
            
            // 5️⃣ JWT 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getDepartment()
            );
            
            String refreshToken = jwtUtil.generateRefreshToken(
                user.getId(),
                user.getUsername()
            );
            
            // 6️⃣ 로그인 성공 처리
            handleSuccessfulLogin(user);
            
            // 7️⃣ 응답 데이터 구성
            LoginResponse response = buildLoginResponse(user, accessToken, refreshToken);
            
            log.info("✅ 로그인 성공: username={}, role={}, userId={}", 
                user.getUsername(), user.getRole(), user.getId());
            
            return response;
            
        } catch (Exception e) {
            // 8️⃣ 로그인 실패 처리
            handleFailedLogin(loginRequest.getUsername(), e);
            throw e;
        }
    }

    /**
     * 🚪 로그아웃 - 토큰 무효화
     * 
     * @param bearerToken Authorization 헤더의 Bearer 토큰
     * @return 로그아웃 결과
     */
    public Map<String, Object> logout(String bearerToken) {
        log.info("🚪 로그아웃 요청 수신");
        
        try {
            // 1️⃣ Bearer 토큰에서 실제 JWT 추출
            String token = jwtUtil.extractTokenFromBearerString(bearerToken);
            
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
            }
            
            // 2️⃣ 토큰에서 사용자 정보 추출
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 3️⃣ 토큰 블랙리스트에 추가
            blacklistedTokens.add(token);
            
            // 4️⃣ 로그아웃 로깅
            logInfo(
                userId,
                "LOGOUT",
                String.format("사용자 로그아웃: %s", username)
            );
            
            // 5️⃣ 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "성공적으로 로그아웃되었습니다.");
            response.put("username", username);
            response.put("logoutTime", LocalDateTime.now());
            
            log.info("✅ 로그아웃 성공: username={}, userId={}", username, userId);
            return response;
            
        } catch (Exception e) {
            log.error("❌ 로그아웃 실패: error={}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "로그아웃 처리 중 오류가 발생했습니다.");
            errorResponse.put("error", e.getMessage());
            
            return errorResponse;
        }
    }

    /**
     * 🔍 토큰 유효성 검증
     * 
     * @param token JWT 토큰
     * @return 토큰 유효성 여부
     */
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        try {
            // 1️⃣ 블랙리스트 확인
            if (isTokenBlacklisted(token)) {
                log.warn("🚫 블랙리스트된 토큰: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                return false;
            }
            
            // 2️⃣ 토큰 파싱 및 기본 검증
            String username = jwtUtil.getUsernameFromToken(token);
            
            // 3️⃣ 사용자 존재 여부 확인
            User user;
            try {
                user = userService.findByUsername(username);
            } catch (EntityNotFoundException e) {
                log.warn("🚫 존재하지 않는 사용자: {}", username);
                return false;
            }
            
            // 4️⃣ 계정 활성화 상태 확인
            if (!user.isActive()) {
                log.warn("🚫 비활성화된 계정: {}", username);
                return false;
            }
            
            // 5️⃣ JWT 유효성 검증
            return jwtUtil.validateAccessToken(token, username);
            
        } catch (Exception e) {
            log.warn("🚫 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 🔄 토큰 갱신
     * 
     * @param refreshToken Refresh Token
     * @return 새로운 Access Token
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        log.info("🔄 토큰 갱신 요청");
        
        try {
            // 1️⃣ Refresh Token 검증
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            
            if (!jwtUtil.validateRefreshToken(refreshToken, username)) {
                throw new BadCredentialsException("유효하지 않은 Refresh Token입니다.");
            }
            
            // 2️⃣ 사용자 조회
            User user = userService.findByUsername(username);
            
            // 3️⃣ 계정 상태 확인
            if (!user.isActive()) {
                throw new DisabledException("비활성화된 계정입니다: " + username);
            }
            
            // 4️⃣ 새로운 Access Token 생성
            String newAccessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getDepartment()
            );
            
            // 5️⃣ 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", newAccessToken);
            response.put("expiresIn", jwtUtil.getTokenRemainingTime(newAccessToken));
            response.put("refreshedAt", LocalDateTime.now());
            
            log.info("✅ 토큰 갱신 성공: username={}", username);
            return response;
            
        } catch (Exception e) {
            log.error("❌ 토큰 갱신 실패: error={}", e.getMessage());
            throw new BadCredentialsException("토큰 갱신에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 👤 현재 로그인한 사용자 정보 조회
     * 
     * @param token JWT 토큰
     * @return 사용자 정보
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentUser(String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("name", user.getDisplayName());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole());
            userInfo.put("department", user.getDepartment());
            userInfo.put("isActive", user.isActive());
            userInfo.put("lastLoginTime", user.getLastLoginTime());
            
            return userInfo;
            
        } catch (Exception e) {
            log.error("❌ 현재 사용자 조회 실패: error={}", e.getMessage());
            throw new RuntimeException("사용자 정보 조회에 실패했습니다: " + e.getMessage());
        }
    }

    // ========================= 내부 헬퍼 메서드들 =========================
    
    /**
     * 로그인 시도 횟수 검증
     */
    private void validateLoginAttempts(String username) {
        int attempts = loginAttempts.getOrDefault(username, 0);
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            // 🔐 보안 로그: 로그인 시도 횟수 초과
            logSecurity(null, "LOGIN_ATTEMPTS_EXCEEDED", 
                String.format("로그인 시도 횟수 초과: username=%s, attempts=%d", username, attempts));
            
            log.warn("🚫 로그인 시도 횟수 초과: username={}, attempts={}", username, attempts);
            throw new BadCredentialsException(
                String.format("로그인 시도 횟수를 초과했습니다. (%d회) 잠시 후 다시 시도해주세요.", MAX_LOGIN_ATTEMPTS)
            );
        }
    }
    
    /**
     * 사용자 조회 및 기본 검증
     */
    private User validateUser(LoginRequest loginRequest) {
        try {
            return userService.findByUsername(loginRequest.getUsername());
        } catch (EntityNotFoundException e) {
            log.warn("🚫 존재하지 않는 사용자: {}", loginRequest.getUsername());
            throw new BadCredentialsException("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }
    }
    
    /**
     * 비밀번호 검증
     */
    private void validatePassword(String rawPassword, String encodedPassword, String username) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            log.warn("🚫 비밀번호 불일치: username={}", username);
            throw new BadCredentialsException("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }
    }
    
    /**
     * 계정 상태 검증
     */
    private void validateAccountStatus(User user) {
        if (!user.isActive()) {
            log.warn("🚫 비활성화된 계정: username={}", user.getUsername());
            throw new DisabledException("비활성화된 계정입니다. 관리자에게 문의하세요.");
        }
    }
    
    /**
     * 로그인 성공 처리
     */
    private void handleSuccessfulLogin(User user) {
        // 로그인 시도 횟수 초기화
        loginAttempts.remove(user.getUsername());
        
        // 마지막 로그인 시간 업데이트
        userService.updateLastLoginTime(user.getId());
        
        // 로그인 성공 로깅
        logInfo(
            user.getId(),
            "LOGIN_SUCCESS",
            String.format("사용자 로그인 성공: %s (%s)", user.getUsername(), user.getRole())
        );
    }
    
    /**
     * 로그인 실패 처리
     */
    private void handleFailedLogin(String username, Exception exception) {
        // 로그인 시도 횟수 증가
        int attempts = loginAttempts.getOrDefault(username, 0) + 1;
        loginAttempts.put(username, attempts);
        
        // 실패 로깅 (사용자 ID가 없으므로 null로 처리)
        logError(
            null,
            "LOGIN_FAILED",
            String.format("로그인 실패: username=%s, attempts=%d, error=%s", 
                username, attempts, exception.getMessage())
        );
        
        log.warn("❌ 로그인 실패: username={}, attempts={}, error={}", 
            username, attempts, exception.getMessage());
    }
    
    /**
     * LoginResponse 구성
     */
    private LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken) {
        return LoginResponse.builder()
            // 🔐 인증 정보
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtUtil.getTokenRemainingTime(accessToken))
            
            // 👤 사용자 기본 정보
            .username(user.getUsername())
            .name(user.getDisplayName())
            .role(user.getRole())
            .department(user.getDepartment())
            
            // 🏭 작업 환경 정보 (기본값 설정)
            .accessibleFacilityIds(Arrays.asList()) // TODO: 실제 권한 기반 설비 목록
            .currentShift(getCurrentShift()) // TODO: 실제 근무 시간 기반 계산
            
            // 📊 권한 정보
            .permissions(getRolePermissions(user.getRole()))
            
            // 🔔 알림 설정 (기본값)
            .alertEnabled(true)
            .alertTypes(getDefaultAlertTypes(user.getRole()))
            
            // 📝 추가 메타데이터
            .lastLoginTime(user.getLastLoginTime())
            .message(String.format("안녕하세요, %s님! SmartMES에 오신 것을 환영합니다.", user.getDisplayName()))
            .passwordChangeRequired(isPasswordChangeRequired(user))
            
            .build();
    }
    
    /**
     * 현재 근무조 계산 (추후 확장)
     */
    private String getCurrentShift() {
        // TODO: 실제 시간 기반 근무조 계산 로직
        int hour = LocalDateTime.now().getHour();
        if (hour >= 6 && hour < 14) return "주간";
        else if (hour >= 14 && hour < 22) return "오후";
        else return "야간";
    }
    
    /**
     * 역할별 권한 목록
     */
    private List<String> getRolePermissions(Role role) {
        switch (role) {
            case ADMIN:
                return Arrays.asList(
                    "USER_MANAGE", "FACILITY_MANAGE", "SENSOR_MANAGE", 
                    "WORK_ORDER_MANAGE", "PRODUCTION_VIEW", "ALERT_MANAGE",
                    "SYSTEM_CONFIG", "DASHBOARD_ADMIN"
                );
            case MANAGER:
                return Arrays.asList(
                    "WORK_ORDER_CREATE", "WORK_ORDER_MANAGE", "PRODUCTION_VIEW", 
                    "ALERT_VIEW", "DASHBOARD_MANAGER", "FACILITY_VIEW"
                );
            case OPERATOR:
                return Arrays.asList(
                    "WORK_ORDER_VIEW", "PRODUCTION_REGISTER", "ALERT_VIEW", 
                    "DASHBOARD_OPERATOR"
                );
            default:
                return Arrays.asList();
        }
    }
    
    /**
     * 역할별 기본 알림 타입
     */
    private List<String> getDefaultAlertTypes(Role role) {
        switch (role) {
            case ADMIN:
                return Arrays.asList("SYSTEM", "SECURITY", "CRITICAL", "WARNING", "INFO");
            case MANAGER:
                return Arrays.asList("CRITICAL", "WARNING", "PRODUCTION");
            case OPERATOR:
                return Arrays.asList("CRITICAL", "WORK_ORDER");
            default:
                return Arrays.asList("CRITICAL");
        }
    }
    
    /**
     * 비밀번호 변경 필요 여부 확인
     */
    private boolean isPasswordChangeRequired(User user) {
        // TODO: 비밀번호 정책에 따른 변경 필요 여부 확인
        // 예: 90일 이상 미변경, 초기 비밀번호 사용 등
        return false;
    }
    
    /**
     * 토큰 블랙리스트 확인
     */
    private boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    // ========================= 추가 확장 메서드들 =========================
    
    /**
     * 🔧 로그인 시도 횟수 초기화 (관리자용)
     */
    public void resetLoginAttempts(String username) {
        loginAttempts.remove(username);
        logInfo(null, "LOGIN_ATTEMPTS_RESET", 
            String.format("로그인 시도 횟수 초기화: username=%s", username));
        log.info("🔧 로그인 시도 횟수 초기화: username={}", username);
    }
    
    /**
     * 📊 현재 로그인 시도 현황 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> getLoginAttempts() {
        return new HashMap<>(loginAttempts);
    }
    
    /**
     * 🧹 만료된 토큰 정리 (스케줄러용)
     */
    public void cleanupExpiredTokens() {
        int beforeSize = blacklistedTokens.size();
        blacklistedTokens.removeIf(token -> {
            try {
                return jwtUtil.isTokenExpired(token);
            } catch (Exception e) {
                return true; // 파싱 불가능한 토큰은 제거
            }
        });
        int afterSize = blacklistedTokens.size();
        int cleaned = beforeSize - afterSize;
        
        if (cleaned > 0) {
            logInfo(null, "TOKEN_CLEANUP", 
                String.format("만료된 토큰 정리: 제거된 토큰 수=%d, 남은 토큰 수=%d", cleaned, afterSize));
        }
        
        log.info("🧹 만료된 토큰 정리 완료: 제거={}, 남은 토큰 수={}", cleaned, afterSize);
    }
    
    /**
     * 🔍 토큰 정보 상세 조회 (디버깅용)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTokenDetails(String token) {
        return jwtUtil.getTokenInfo(token);
    }

    // ========================= 로깅 메서드들 =========================

    /**
     * 🔍 정보성 로그 기록 (INFO)
     * 
     * @param userId 사용자 ID (null 가능 - 시스템 로그의 경우)
     * @param action 액션 코드 (예: "LOGIN_SUCCESS", "LOGOUT")
     * @param message 상세 메시지
     */
    private void logInfo(Long userId, String action, String message) {
        createAndSaveLog(userId, "INFO", action, message);
    }

    /**
     * ❌ 오류 로그 기록 (ERROR)
     * 
     * @param userId 사용자 ID (null 가능 - 시스템 오류의 경우)
     * @param action 액션 코드 (예: "LOGIN_FAILED", "TOKEN_VALIDATION_FAILED")
     * @param message 오류 메시지
     */
    private void logError(Long userId, String action, String message) {
        createAndSaveLog(userId, "ERROR", action, message);
    }

    /**
     * ⚠️ 경고 로그 기록 (WARNING)
     * 
     * @param userId 사용자 ID
     * @param action 액션 코드
     * @param message 경고 메시지
     */
    private void logWarning(Long userId, String action, String message) {
        createAndSaveLog(userId, "WARNING", action, message);
    }

    /**
     * 🔐 보안 로그 기록 (SECURITY)
     * 
     * @param userId 사용자 ID
     * @param action 보안 액션 (예: "UNAUTHORIZED_ACCESS", "SUSPICIOUS_ACTIVITY")
     * @param message 보안 이벤트 상세
     */
    private void logSecurity(Long userId, String action, String message) {
        createAndSaveLog(userId, "SECURITY", action, message);
    }

    /**
     * 로그 엔트리 생성 및 저장 (공통 메서드)
     * 
     * @param userId 사용자 ID (null 가능)
     * @param logLevel 로그 레벨 (INFO, ERROR, WARNING, SECURITY)
     * @param action 액션 코드
     * @param message 메시지
     */
    private void createAndSaveLog(Long userId, String logLevel, String action, String message) {
        try {
            // 사용자 조회 (userId가 null이 아닌 경우에만)
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    log.warn("⚠️ 로그 기록 중 사용자를 찾을 수 없음: userId={}", userId);
                }
            }

            // LogEntry 생성
            LogEntry logEntry = LogEntry.builder()
                    .user(user)
                    .action(String.format("[%s] %s", logLevel, action))
                    .targetTable("AUTH_SYSTEM")
                    .targetId(userId != null ? userId : 0L)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            // 로그 저장
            logEntryRepository.save(logEntry);

            // 콘솔 로깅 (개발/디버깅용)
            String userInfo = user != null ? user.getUsername() : "SYSTEM";
            log.debug("📋 인증 로그: [{}] {} - {} (user: {})", logLevel, action, message, userInfo);

        } catch (Exception e) {
            // 로그 저장 실패 시에도 시스템이 중단되지 않도록 처리
            log.error("❌ 인증 로그 기록 실패: action={}, message={}, error={}", action, message, e.getMessage());
        }
    }
}
