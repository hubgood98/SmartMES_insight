package com.smartfactory.smartmes_insight.config;

import com.smartfactory.smartmes_insight.domain.user.Role;
import com.smartfactory.smartmes_insight.domain.user.User;
import com.smartfactory.smartmes_insight.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 🚀 애플리케이션 시작 시 초기 데이터 생성
 * 
 * 첫 실행 시 기본 관리자 계정을 자동으로 생성합니다.
 * 이미 관리자가 존재하면 생성하지 않습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createInitialAdminIfNotExists();
    }

    private void createInitialAdminIfNotExists() {
        // 이미 관리자가 있는지 확인
        long adminCount = userRepository.countByRole(Role.ADMIN);
        
        if (adminCount == 0) {
            log.info("🔧 초기 관리자 계정이 없습니다. 기본 관리자를 생성합니다...");
            
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@smartmes.com")
                    .realName("시스템 관리자")
                    .role(Role.ADMIN)
                    .phone("010-0000-0000")
                    .department("IT")
                    .isActive(true)
                    .build();
            
            userRepository.save(admin);
            
            log.info("✅ 초기 관리자 계정이 생성되었습니다.");
            log.info("👤 사용자명: admin");
            log.info("🔑 비밀번호: admin123");
            log.info("⚠️  보안을 위해 초기 비밀번호를 변경하세요!");
            
        } else {
            log.info("✅ 관리자 계정이 이미 존재합니다. ({}명)", adminCount);
        }
    }
}
