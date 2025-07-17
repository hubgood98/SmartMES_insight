package com.smartfactory.smartmes_insight.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "사용자 수정 요청 DTO")
public class UserUpdateRequest {

    @Size(min = 8, max = 100, message = "비밀번호는 8~100자여야 합니다")
    @Schema(description = "비밀번호 (변경 시에만 입력)", example = "newpassword123")
    private String password;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Schema(description = "이메일", example = "john@example.com")
    private String email;

    @Size(max = 100, message = "실명은 100자를 초과할 수 없습니다")
    @Schema(description = "실명", example = "홍길동")
    private String realName;

    @Pattern(regexp = "^(ADMIN|MANAGER|OPERATOR)$", message = "역할은 ADMIN, MANAGER, OPERATOR 중 하나여야 합니다")
    @Schema(description = "역할", example = "OPERATOR", allowableValues = {"ADMIN", "MANAGER", "OPERATOR"})
    private String role;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "부서", example = "생산부")
    private String department;

    @Schema(description = "활성 상태", example = "true")
    private Boolean active;
}
