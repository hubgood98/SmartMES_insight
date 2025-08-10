package com.smartfactory.smartmes_insight.dto.auth;

import com.smartfactory.smartmes_insight.domain.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    // 🔐 인증 정보
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;

    // 👤 사용자 기본 정보 (userId 제거!)
    private String username;
    private String name;
    private Role role;

    // 🏭 작업 환경 정보
    private List<Long> accessibleFacilityIds;
    private String currentShift;
    private String department;

    // 📊 대시보드 설정
    private List<String> permissions;

    // 🔔 실시간 알림 설정
    private boolean alertEnabled;
    private List<String> alertTypes;

    // 📝 추가 메타데이터
    private LocalDateTime lastLoginTime;
    private String message;
    private boolean passwordChangeRequired;
}