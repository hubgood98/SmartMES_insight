package com.smartfactory.smartmes_insight.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 응답 DTO")
public class LoginResponse {

    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "JWT 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "토큰 만료 시간 (초)", example = "3600")
    private Long expiresIn;

    @Schema(description = "사용자 정보")
    private UserInfo userInfo;

    @Schema(description = "로그인 시간", example = "2025-01-20T10:30:00")
    private LocalDateTime loginTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "로그인 사용자 정보")
    public static class UserInfo {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "사용자명", example = "john_doe")
        private String username;

        @Schema(description = "실명", example = "홍길동")
        private String realName;

        @Schema(description = "역할", example = "OPERATOR")
        private String role;

        @Schema(description = "이메일", example = "john@example.com")
        private String email;

        @Schema(description = "부서", example = "생산부")
        private String department;
    }
}
