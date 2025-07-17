package com.smartfactory.smartmes_insight.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    // 사용자명 존재 여부 확인
    boolean existsByUsername(String username);
    
    // 이메일 조회 (중복 검증용)
    Optional<User> findByEmail(String email);
    
    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);
    
    // 역할별 사용자 조회
    List<User> findByRole(Role role);
    
    // 역할별 사용자 수 조회
    long countByRole(Role role);
}
