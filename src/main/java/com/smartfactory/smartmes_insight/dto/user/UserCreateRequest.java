package com.smartfactory.smartmes_insight.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "사용자 생성 요청 DTO")
public class UserCreateRequest {

    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 50, message = "사용자명은 3~50자여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "사용자명은 영문, 숫자, 언더스코어만 가능합니다")
    @Schema(description = "사용자명", example = "john_doe", required = true)
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8~100자여야 합니다")
    @Schema(description = "비밀번호", example = "password123", required = true)
    private String password;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "이메일", example = "john@example.com", required = true)
    private String email;

    @NotBlank(message = "실명은 필수입니다")
    @Size(max = 100, message = "실명은 100자를 초과할 수 없습니다")
    @Schema(description = "실명", example = "홍길동", required = true)
    private String realName;

    @NotBlank(message = "역할은 필수입니다")
    @Pattern(regexp = "^(ADMIN|MANAGER|OPERATOR)$", message = "역할은 ADMIN, MANAGER, OPERATOR 중 하나여야 합니다")
    @Schema(description = "역할", example = "OPERATOR", allowableValues = {"ADMIN", "MANAGER", "OPERATOR"}, required = true)
    private String role;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "부서", example = "생산부")
    private String department;
}
