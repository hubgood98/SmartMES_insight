package com.smartfactory.smartmes_insight.service;

import com.smartfactory.smartmes_insight.domain.user.Role;
import com.smartfactory.smartmes_insight.domain.user.User;
import com.smartfactory.smartmes_insight.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
