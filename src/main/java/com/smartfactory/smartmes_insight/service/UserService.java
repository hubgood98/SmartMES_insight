package com.smartfactory.smartmes_insight.service;

import com.smartfactory.smartmes_insight.domain.user.Role;
import com.smartfactory.smartmes_insight.domain.user.User;
import com.smartfactory.smartmes_insight.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User save(User user) {
        return userRepository.save(user);
    }

    public void changePassword(Long userId, String rawNewPassword) {
        User user = getUserOrThrow(userId);

        String encodedPassword = passwordEncoder.encode(rawNewPassword);
        user.changePassword(encodedPassword);

    }

    @Transactional
    public void updateUserRole(Long userId, Role newRole) {
        User user = getUserOrThrow(userId);
        user.changeRole(newRole);
    }

    //이 메서드는 외부 서비스 메서드나 테스트 등에 활용할거임
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
    }
    
    // ========================= 알림 시스템용 메서드들 =========================
    
    /**
     * 관리자 역할 사용자들의 ID 목록 조회
     */
    @Transactional(readOnly = true)
    public List<String> getManagerUserIds() {
        return userRepository.findByRole(Role.MANAGER)
                .stream()
                .filter(User::isActive) // 활성 사용자만
                .map(user -> user.getId().toString())
                .collect(Collectors.toList());
    }
    
    /**
     * 작업자 역할 사용자들의 ID 목록 조회
     */
    @Transactional(readOnly = true)
    public List<String> getOperatorUserIds() {
        return userRepository.findByRole(Role.OPERATOR)
                .stream()
                .filter(User::isActive) // 활성 사용자만
                .map(user -> user.getId().toString())
                .collect(Collectors.toList());
    }
    
    /**
     * 관리자들의 이메일 목록 조회 (알림용)
     */
    @Transactional(readOnly = true)
    public List<String> getManagerEmails() {
        return userRepository.findByRole(Role.MANAGER)
                .stream()
                .filter(User::isActive) // 활성 사용자만
                .filter(User::hasValidEmail) // 유효한 이메일만
                .map(User::getEmail)
                .collect(Collectors.toList());
    }
    
    /**
     * 작업자들의 이메일 목록 조회 (알림용)
     */
    @Transactional(readOnly = true)
    public List<String> getOperatorEmails() {
        return userRepository.findByRole(Role.OPERATOR)
                .stream()
                .filter(User::isActive) // 활성 사용자만
                .filter(User::hasValidEmail) // 유효한 이메일만
                .map(User::getEmail)
                .collect(Collectors.toList());
    }
    
    /**
     * 긴급 연락처 전화번호 목록 조회 (SMS 알림용)
     */
    @Transactional(readOnly = true)
    public List<String> getEmergencyPhoneNumbers() {
        return userRepository.findByRole(Role.MANAGER)
                .stream()
                .filter(User::isActive) // 활성 사용자만
                .filter(User::hasValidPhone) // 유효한 전화번호만
                .map(User::getPhone)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 역할의 사용자 수 조회
     */
    @Transactional(readOnly = true)
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }
    
    /**
     * 활성 사용자들의 사용자명 목록 조회 (WebSocket 세션 관리용)
     */
    @Transactional(readOnly = true)
    public List<String> getActiveUsernames() {
        return userRepository.findAll()
                .stream()
                .filter(User::isActive) // 활성 사용자만
                .map(User::getUsername)
                .collect(Collectors.toList());
    }
    
    // ========================= 사용자 관리 메서드들 =========================
    
    /**
     * 사용자 프로필 업데이트
     */
    public void updateUserProfile(Long userId, String email, String phone, String realName, String department) {
        User user = getUserOrThrow(userId);
        user.updateProfile(email, phone, realName, department);
    }
    
    /**
     * 사용자 활성화
     */
    public void activateUser(Long userId) {
        User user = getUserOrThrow(userId);
        user.activate();
    }
    
    /**
     * 사용자 비활성화
     */
    public void deactivateUser(Long userId) {
        User user = getUserOrThrow(userId);
        user.deactivate();
    }
    
    /**
     * 이메일 변경
     */
    public void updateUserEmail(Long userId, String email) {
        User user = getUserOrThrow(userId);
        
        // 🔍 이메일 중복 검증 (도메인 레벨에서는 불가능한 검증)
        if (email != null && !email.trim().isEmpty()) {
            String normalizedEmail = email.trim().toLowerCase();
            Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new IllegalStateException("이미 사용 중인 이메일입니다: " + normalizedEmail);
            }
        }
        
        user.updateEmail(email);
    }
    
    /**
     * 전화번호 변경
     */
    public void updateUserPhone(Long userId, String phone) {
        User user = getUserOrThrow(userId);
        user.updatePhone(phone);
    }
    
    /**
     * 활성 사용자 수 조회
     */
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.findAll()
                .stream()
                .filter(User::isActive)
                .count();
    }
    
    /**
     * 부서별 사용자 조회
     */
    @Transactional(readOnly = true)
    public List<User> findByDepartment(String department) {
        return userRepository.findAll()
                .stream()
                .filter(User::isActive)
                .filter(user -> department.equals(user.getDepartment()))
                .collect(Collectors.toList());
    }
    
    /**
     * 전체 관리자(ADMIN + MANAGER) 목록 조회
     */
    @Transactional(readOnly = true)
    public List<String> getAllManagerUserIds() {
        List<String> adminIds = userRepository.findByRole(Role.ADMIN)
                .stream()
                .filter(User::isActive)
                .map(user -> user.getId().toString())
                .collect(Collectors.toList());
                
        List<String> managerIds = getManagerUserIds();
        
        adminIds.addAll(managerIds);
        return adminIds;
    }
}
