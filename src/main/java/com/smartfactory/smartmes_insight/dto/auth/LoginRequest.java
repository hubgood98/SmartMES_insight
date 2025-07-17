package com.smartfactory.smartmes_insight.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청 DTO")
public class LoginRequest {

    @NotBlank(message = "사용자명은 필수입니다")
    @Schema(description = "사용자명", example = "john_doe", required = true)
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "비밀번호", example = "password123", required = true)
    private String password;

    @Schema(description = "로그인 상태 유지", example = "true", defaultValue = "false")
    private Boolean rememberMe = false;
}
