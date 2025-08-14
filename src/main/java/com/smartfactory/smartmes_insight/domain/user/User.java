package com.smartfactory.smartmes_insight.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 100)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(length = 100)
    private String realName; // 실명
    
    @Column(length = 50)
    private String department; // 부서

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    // ========================= 비즈니스 메서드들 =========================
    
    /**
     * 비밀번호 변경 (비즈니스 규칙 적용)
     */
    public void changePassword(String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 역할 변경 (권한 검증 포함)
     */
    public void changeRole(Role newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("역할은 필수입니다.");
        }
        
        // 🔒 비즈니스 규칙: ADMIN은 다른 ADMIN이 되거나 해제될 수 없음 (보안)
        if (this.role == Role.ADMIN && newRole != Role.ADMIN) {
            throw new IllegalStateException("최고관리자 권한은 해제할 수 없습니다.");
        }
        
        this.role = newRole;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 프로필 업데이트 (유효성 검증 포함)
     */
    public void updateProfile(String email, String phone, String realName, String department) {
        // 📧 이메일 유효성 검증
        if (email != null && !email.trim().isEmpty()) {
            if (isInvalidEmailFormat(email)) {
                throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다: " + email);
            }
            this.email = email.trim().toLowerCase(); // 정규화
        }
        
        // 📞 전화번호 유효성 검증
        if (phone != null && !phone.trim().isEmpty()) {
            String normalizedPhone = normalizePhoneNumber(phone);
            if (isInvalidPhoneFormat(normalizedPhone)) {
                throw new IllegalArgumentException("올바른 전화번호 형식이 아닙니다: " + phone);
            }
            this.phone = normalizedPhone;
        }
        
        // 👤 실명 검증
        if (realName != null && !realName.trim().isEmpty()) {
            if (realName.trim().length() < 2) {
                throw new IllegalArgumentException("실명은 최소 2자 이상이어야 합니다.");
            }
            this.realName = realName.trim();
        }
        
        // 🏢 부서 검증
        if (department != null && !department.trim().isEmpty()) {
            this.department = department.trim();
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 안전한 이메일 변경 (중복 검증은 서비스 레이어에서)
     */
    public void updateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        
        if (isInvalidEmailFormat(email)) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다: " + email);
        }
        
        String normalizedEmail = email.trim().toLowerCase();
        
        // 🔄 동일한 이메일로 변경하는 경우 무시
        if (normalizedEmail.equals(this.email)) {
            return;
        }
        
        this.email = normalizedEmail;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 안전한 전화번호 변경
     */
    public void updatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            this.phone = null; // 전화번호는 선택사항
            this.updatedAt = LocalDateTime.now();
            return;
        }
        
        String normalizedPhone = normalizePhoneNumber(phone);
        
        if (isInvalidPhoneFormat(normalizedPhone)) {
            throw new IllegalArgumentException("올바른 전화번호 형식이 아닙니다: " + phone);
        }
        
        // 🔄 동일한 전화번호로 변경하는 경우 무시
        if (normalizedPhone.equals(this.phone)) {
            return;
        }
        
        this.phone = normalizedPhone;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 사용자 활성화 (비즈니스 규칙 적용)
     */
    public void activate() {
        if (Boolean.TRUE.equals(this.isActive)) {
            return; // 이미 활성 상태면 무시
        }
        
        // 🔒 비즈니스 규칙: 필수 정보가 없으면 활성화 불가
        if (this.username == null || this.username.trim().isEmpty()) {
            throw new IllegalStateException("사용자명이 없어 활성화할 수 없습니다.");
        }
        
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 사용자 비활성화 (비즈니스 규칙 적용)
     */
    public void deactivate() {
        if (Boolean.FALSE.equals(this.isActive)) {
            return; // 이미 비활성 상태면 무시
        }
        
        // 🔒 비즈니스 규칙: ADMIN은 비활성화 불가
        if (this.role == Role.ADMIN) {
            throw new IllegalStateException("최고관리자는 비활성화할 수 없습니다.");
        }
        
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========================= 내부 유틸리티 메서드들 =========================
    
    /**
     * 이메일 형식 검증 (부정형 - 잘못된 형식인지 확인)
     */
    private boolean isInvalidEmailFormat(String email) {
        if (email == null) return true;
        return !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * 전화번호 정규화 (하이픈 제거, 국가번호 처리)
     */
    private String normalizePhoneNumber(String phone) {
        if (phone == null) return null;
        
        // 하이픈, 공백, 괄호 제거
        String normalized = phone.replaceAll("[\\s()-]", "");
        
        // 국내 번호 처리 (010으로 시작하는 경우)
        if (normalized.startsWith("010")) {
            return normalized.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        }
        
        // 일반 번호 (지역번호 등)
        if (normalized.matches("^0\\d{1,2}\\d{7,8}$")) {
            return normalized.replaceFirst("(0\\d{1,2})(\\d{3,4})(\\d{4})", "$1-$2-$3");
        }
        
        return normalized;
    }
    
    /**
     * 전화번호 형식 검증 (부정형 - 잘못된 형식인지 확인)
     */
    private boolean isInvalidPhoneFormat(String phone) {
        if (phone == null) return true;
        // 한국 전화번호 패턴 (010-1234-5678, 02-123-4567 등)
        return !phone.matches("^(010-\\d{4}-\\d{4}|0\\d{1,2}-\\d{3,4}-\\d{4})$");
    }
    
    /**
     * 활성화 상태 확인
     */
    public boolean isActive() {
        return this.isActive != null ? this.isActive : false;
    }
    
    /**
     * 이메일 유효성 확인
     */
    public boolean hasValidEmail() {
        return this.email != null && !this.email.trim().isEmpty() && this.email.contains("@");
    }
    
    /**
     * 전화번호 유효성 확인
     */
    public boolean hasValidPhone() {
        return this.phone != null && !this.phone.trim().isEmpty() && this.phone.matches("^[0-9-]+$");
    }
    
    /**
     * 관리자 권한 확인
     */
    public boolean isManager() {
        return Role.MANAGER.equals(this.role) || Role.ADMIN.equals(this.role);
    }
    
    /**
     * 작업자 권한 확인
     */
    public boolean isOperator() {
        return Role.OPERATOR.equals(this.role);
    }
    
    /**
     * 최고관리자 권한 확인
     */
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }
    
    /**
     * 사용자 표시명 (실명이 있으면 실명, 없으면 사용자명)
     */
    public String getDisplayName() {
        return (realName != null && !realName.trim().isEmpty()) ? realName : username;
    }
    
    /**
     * 연락처 정보 요약
     */
    public String getContactInfo() {
        StringBuilder contact = new StringBuilder();
        if (hasValidEmail()) {
            contact.append("📧 ").append(email);
        }
        if (hasValidPhone()) {
            if (!contact.isEmpty()) {
                contact.append(" | ");
            }
            contact.append("📞 ").append(phone);
        }
        return contact.toString();
    }
    
    /**
     * 사용자 정보 요약
     */
    public String getSummary() {
        return String.format("[%s] %s (%s) - %s", 
                role.name(), 
                getDisplayName(), 
                username, 
                isActive() ? "활성" : "비활성");
    }
    
    // ========================= UserService에서 필요한 메서드들 =========================
    
    /**
     * 실명 변경
     */
    public void updateRealName(String realName) {
        if (realName != null && !realName.trim().isEmpty()) {
            if (realName.trim().length() < 2) {
                throw new IllegalArgumentException("실명은 최소 2자 이상이어야 합니다.");
            }
            this.realName = realName.trim();
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 부서 변경
     */
    public void updateDepartment(String department) {
        if (department != null) {
            this.department = department.trim().isEmpty() ? null : department.trim();
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 마지막 로그인 시간 업데이트
     * 로그인 성공 시 호출되는 메서드
     */
    public void updateLastLoginTime() {
        LocalDateTime now = LocalDateTime.now();
        this.lastLoginTime = now;
        this.updatedAt = now;
    }
    
    /**
     * 마지막 로그인 시간을 특정 시간으로 설정
     * 
     * @param loginTime 설정할 로그인 시간
     */
    public void updateLastLoginTime(LocalDateTime loginTime) {
        if (loginTime != null) {
            this.lastLoginTime = loginTime;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
    
    @PreUpdate  
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
