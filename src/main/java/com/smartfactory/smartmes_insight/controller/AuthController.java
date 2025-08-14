package com.smartfactory.smartmes_insight.controller;

import com.smartfactory.smartmes_insight.common.ApiResponse;
import com.smartfactory.smartmes_insight.dto.auth.LoginRequest;
import com.smartfactory.smartmes_insight.dto.auth.LoginResponse;
import com.smartfactory.smartmes_insight.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "🔐 인증 관리", description = "로그인, 로그아웃, 토큰 관리 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "사용자명과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody @Parameter(description = "로그인 요청 데이터") LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "로그인 성공"));
    }

    @Operation(summary = "로그아웃", description = "현재 토큰을 무효화하여 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, Object>>> logout(
            @RequestHeader("Authorization") @Parameter(description = "Bearer JWT 토큰") String bearerToken) {
        Map<String, Object> result = authService.logout(bearerToken);
        return ResponseEntity.ok(ApiResponse.success(result, "로그아웃 성공"));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken(
            @RequestBody @Parameter(description = "Refresh Token") Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        Map<String, Object> result = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(result, "토큰 갱신 성공"));
    }

    @Operation(summary = "현재 사용자 정보", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            @RequestHeader("Authorization") @Parameter(description = "Bearer JWT 토큰") String bearerToken) {
        String token = bearerToken.substring(7); // "Bearer " 제거
        Map<String, Object> userInfo = authService.getCurrentUser(token);
        return ResponseEntity.ok(ApiResponse.success(userInfo, "사용자 정보 조회 성공"));
    }

    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증합니다.")
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            @RequestHeader("Authorization") @Parameter(description = "Bearer JWT 토큰") String bearerToken) {
        String token = bearerToken.substring(7); // "Bearer " 제거
        boolean isValid = authService.validateToken(token);
        
        Map<String, Object> result = Map.of(
            "valid", isValid,
            "message", isValid ? "유효한 토큰입니다." : "유효하지 않은 토큰입니다."
        );
        
        return ResponseEntity.ok(ApiResponse.success(result, "토큰 검증 완료"));
    }
}
