package com.smartfactory.smartmes_insight.dto.user;

import com.smartfactory.smartmes_insight.domain.user.User;
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
@Schema(description = "사용자 응답 DTO")
public class UserResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "사용자명", example = "john_doe")
    private String username;

    @Schema(description = "이메일", example = "john@example.com")
    private String email;

    @Schema(description = "실명", example = "홍길동")
    private String realName;

    @Schema(description = "역할", example = "OPERATOR")
    private String role;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "부서", example = "생산부")
    private String department;

    @Schema(description = "활성 상태", example = "true")
    private Boolean active;

    @Schema(description = "생성일시", example = "2025-01-20T10:30:00")
    private LocalDateTime createdDate;

    @Schema(description = "수정일시", example = "2025-01-20T14:30:00")
    private LocalDateTime updatedDate;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .realName(user.getRealName())
                .role(user.getRole())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .active(user.getActive())
                .createdDate(user.getCreatedDate())
                .updatedDate(user.getUpdatedDate())
                .build();
    }
}
