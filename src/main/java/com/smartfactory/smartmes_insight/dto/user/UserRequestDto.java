package com.smartfactory.smartmes_insight.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 🏭 MES 사용자 계정 요청 DTO
 * 
 * 실제 MES 환경에서는 관리자가 직접 계정을 생성하는 것이 아니라,
 * 부서장이나 담당자가 계정 생성을 요청하고 관리자가 승인하는 방식으로 운영됩니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {
    
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 20, message = "사용자명은 3-20자 사이여야 합니다")
    private String username;
    
    @NotBlank(message = "실명은 필수입니다")
    @Size(max = 50, message = "실명은 50자를 초과할 수 없습니다")
    private String realName;
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이어야 합니다")
    private String email;
    
    @NotBlank(message = "전화번호는 필수입니다")
    private String phone;
    
    @NotBlank(message = "부서는 필수입니다")
    private String department;
    
    @NotBlank(message = "요청 역할은 필수입니다")
    private String requestedRole; // MANAGER, OPERATOR만 가능 (ADMIN 제외)
    
    @NotBlank(message = "사원번호는 필수입니다")
    private String employeeId;
    
    @NotBlank(message = "직급은 필수입니다")
    private String position;
    
    @NotBlank(message = "입사일은 필수입니다")
    private String hireDate;
    
    @Size(max = 500, message = "요청 사유는 500자를 초과할 수 없습니다")
    private String requestReason; // 계정 필요 사유
    
    @NotBlank(message = "승인자는 필수입니다")
    private String approverName; // 승인자 (부서장 등)
    
    private String approverEmail;
    
    // 추가 정보
    private String workShift; // 근무조 (주간/야간/교대)
    private String accessLevel; // 접근 레벨 (일반/제한/특별)
    private String emergencyContact; // 비상연락처
}
