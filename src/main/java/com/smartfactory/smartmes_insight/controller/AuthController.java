package com.smartfactory.smartmes_insight.controller;

import com.smartfactory.smartmes_insight.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "🔐 인증 관리", description = "로그인, 로그아웃, 토큰 관리 API")
public class AuthController {

    // TODO: AuthService 주입 예정
    // private final AuthService authService;
    
    @Operation(summary = "로그인", description = "사용자 로그인을 수행하고 JWT 토큰을 반환합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(
            @RequestParam @Parameter(description = "사용자명") String username,
            @RequestParam @Parameter(description = "비밀번호") String password) {
        
        // TODO: AuthService 구현 후 활성화
        // LoginResponse loginResponse = authService.login(username, password);
        // return ResponseEntity.ok(ApiResponse.success(loginResponse, "로그인 성공"));
        
        // 임시 응답
        return ResponseEntity.ok(ApiResponse.success(null, "로그인 API - 구현 예정"));
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 수행합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        
        // TODO: AuthService 구현 후 활성화
        // authService.logout();
        // return ResponseEntity.ok(ApiResponse.success(null, "로그아웃 성공"));
        
        // 임시 응답
        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃 API - 구현 예정"));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Object>> refreshToken(
            @RequestParam @Parameter(description = "Refresh Token") String refreshToken) {
        
        // TODO: AuthService 구현 후 활성화
        // TokenResponse tokenResponse = authService.refreshToken(refreshToken);
        // return ResponseEntity.ok(ApiResponse.success(tokenResponse, "토큰 갱신 성공"));
        
        // 임시 응답
        return ResponseEntity.ok(ApiResponse.success(null, "토큰 갱신 API - 구현 예정"));
    }

    @Operation(summary = "토큰 검증", description = "현재 토큰의 유효성을 검증합니다.")
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Object>> validateToken() {
        
        // TODO: AuthService 구현 후 활성화
        // UserInfo userInfo = authService.getCurrentUser();
        // return ResponseEntity.ok(ApiResponse.success(userInfo, "토큰 유효"));
        
        // 임시 응답
        return ResponseEntity.ok(ApiResponse.success(null, "토큰 검증 API - 구현 예정"));
    }
}
