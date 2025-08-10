package com.smartfactory.smartmes_insight.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 토큰 생성, 검증, 파싱을 위한 유틸리티 클래스
 * SmartMES Insight 프로젝트의 인증 시스템에서 사용
 *
 * @author SmartMES Team
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // JWT 설정값들 (application.properties에서 주입)
    @Value("${jwt.secret:smartmes-insight-2024-secret-key-for-jwt-token-generation}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24시간 (밀리초)
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7일 (밀리초)
    private Long refreshExpiration;

    // JWT 클레임 키 상수들
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_DEPARTMENT = "department";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";

    // 토큰 타입
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    /**
     * 시크릿 키 생성 (HMAC SHA 알고리즘용)
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * 사용자 정보로부터 Access Token 생성
     *
     * @param userId 사용자 ID
     * @param username 사용자명
     * @param role 사용자 역할 (ADMIN, MANAGER, OPERATOR)
     * @param department 부서명
     * @return JWT Access Token
     */
    public String generateAccessToken(Long userId, String username, String role, String department) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_ROLE, role);
        claims.put(CLAIM_DEPARTMENT, department);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);

        return createToken(claims, username, jwtExpiration);
    }

    /**
     * Refresh Token 생성
     *
     * @param userId 사용자 ID
     * @param username 사용자명
     * @return JWT Refresh Token
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);

        return createToken(claims, username, refreshExpiration);
    }

    /**
     * JWT 토큰 생성 공통 메서드
     *
     * @param claims 토큰에 포함할 클레임들
     * @param subject 토큰 주체 (보통 username)
     * @param expiration 만료 시간 (밀리초)
     * @return JWT 토큰
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        try {
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .setIssuer("SmartMES-Insight")
                    .signWith(getSigningKey())
                    .compact();

            logger.debug("JWT 토큰 생성 성공: subject={}, type={}",
                    subject, claims.get(CLAIM_TOKEN_TYPE));
            return token;

        } catch (Exception e) {
            logger.error("JWT 토큰 생성 실패: subject={}, error={}", subject, e.getMessage());
            throw new RuntimeException("토큰 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 토큰에서 사용자명 추출
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get(CLAIM_USER_ID, Long.class);
    }

    /**
     * 토큰에서 사용자 역할 추출
     */
    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get(CLAIM_ROLE, String.class);
    }

    /**
     * 토큰에서 부서명 추출
     */
    public String getDepartmentFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get(CLAIM_DEPARTMENT, String.class);
    }

    /**
     * 토큰에서 토큰 타입 추출
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get(CLAIM_TOKEN_TYPE, String.class);
    }

    /**
     * 토큰에서 만료 시간 추출
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 토큰에서 발급 시간 추출
     */
    public Date getIssuedAtFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    /**
     * 토큰에서 특정 클레임 추출
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 토큰에서 모든 클레임 추출
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("만료된 JWT 토큰: {}", e.getMessage());
            throw new RuntimeException("토큰이 만료되었습니다.", e);
        } catch (UnsupportedJwtException e) {
            logger.warn("지원되지 않는 JWT 토큰: {}", e.getMessage());
            throw new RuntimeException("지원되지 않는 토큰 형식입니다.", e);
        } catch (MalformedJwtException e) {
            logger.warn("잘못된 형식의 JWT 토큰: {}", e.getMessage());
            throw new RuntimeException("잘못된 토큰 형식입니다.", e);
        } catch (SignatureException e) {
            logger.warn("JWT 서명 검증 실패: {}", e.getMessage());
            throw new RuntimeException("토큰 서명이 유효하지 않습니다.", e);
        } catch (IllegalArgumentException e) {
            logger.warn("JWT 토큰이 비어있음: {}", e.getMessage());
            throw new RuntimeException("토큰이 비어있습니다.", e);
        }
    }

    /**
     * 토큰 만료 확인
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            logger.warn("토큰 만료 확인 중 오류: {}", e.getMessage());
            return true; // 오류 시 만료된 것으로 처리
        }
    }

    /**
     * Access Token 유효성 검증
     */
    public Boolean validateAccessToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            final String tokenType = getTokenTypeFromToken(token);

            return (username.equals(tokenUsername)
                    && TOKEN_TYPE_ACCESS.equals(tokenType)
                    && !isTokenExpired(token));

        } catch (Exception e) {
            logger.warn("Access Token 검증 실패: token={}, username={}, error={}",
                    token.substring(0, Math.min(token.length(), 20)) + "...", username, e.getMessage());
            return false;
        }
    }

    /**
     * Refresh Token 유효성 검증
     */
    public Boolean validateRefreshToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            final String tokenType = getTokenTypeFromToken(token);

            return (username.equals(tokenUsername)
                    && TOKEN_TYPE_REFRESH.equals(tokenType)
                    && !isTokenExpired(token));

        } catch (Exception e) {
            logger.warn("Refresh Token 검증 실패: username={}, error={}", username, e.getMessage());
            return false;
        }
    }

    /**
     * 토큰의 남은 유효 시간 계산 (밀리초)
     */
    public Long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            logger.warn("토큰 남은 시간 계산 실패: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * 토큰 만료까지 남은 시간이 특정 시간 이하인지 확인
     * (토큰 갱신이 필요한지 판단하는 데 사용)
     */
    public Boolean isTokenNearExpiry(String token, long thresholdMinutes) {
        try {
            long remainingTime = getTokenRemainingTime(token);
            long thresholdMillis = thresholdMinutes * 60 * 1000;
            return remainingTime <= thresholdMillis;
        } catch (Exception e) {
            logger.warn("토큰 만료 임박 확인 실패: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 사용자 정보가 담긴 Map으로 토큰 정보 추출
     */
    public Map<String, Object> getTokenInfo(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Map<String, Object> tokenInfo = new HashMap<>();

            tokenInfo.put("userId", claims.get(CLAIM_USER_ID));
            tokenInfo.put("username", claims.getSubject());
            tokenInfo.put("role", claims.get(CLAIM_ROLE));
            tokenInfo.put("department", claims.get(CLAIM_DEPARTMENT));
            tokenInfo.put("tokenType", claims.get(CLAIM_TOKEN_TYPE));
            tokenInfo.put("issuedAt", claims.getIssuedAt());
            tokenInfo.put("expiration", claims.getExpiration());
            tokenInfo.put("issuer", claims.getIssuer());

            return tokenInfo;

        } catch (Exception e) {
            logger.error("토큰 정보 추출 실패: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Bearer 토큰에서 실제 JWT 토큰 추출
     */
    public String extractTokenFromBearerString(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }

    /**
     * 토큰 무효화 여부 확인을 위한 블랙리스트 체크
     * (실제 구현에서는 Redis 등을 사용하여 블랙리스트 관리)
     */
    public Boolean isTokenBlacklisted(String token) {
        // TODO: Redis 또는 데이터베이스를 사용한 블랙리스트 구현
        // 현재는 항상 false 반환 (블랙리스트 기능 비활성화)
        return false;
    }
}