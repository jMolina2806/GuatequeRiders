package com.guatequeriders.api.users.service;

import com.guatequeriders.api.common.exception.DuplicateResourceException;
import com.guatequeriders.api.legal.LegalVersions;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class UsersService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UsersService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String username, String email, String phone, String password) {
        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String normalizedPhone = normalizePhone(phone);

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new DuplicateResourceException("Username already exists: " + normalizedUsername);
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("Email already registered: " + normalizedEmail);
        }
        if (userRepository.existsByPhone(normalizedPhone)) {
            throw new DuplicateResourceException("Phone already registered: " + normalizedPhone);
        }

        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.USER);
        user.setCreatedAt(now);
        user.setPrivacyAcknowledgedAt(now);
        user.setPrivacyPolicyVersion(LegalVersions.PRIVACY_POLICY);
        user.setMembershipActive(false);
        user.setMembershipVersion(0);

        return userRepository.save(user);
    }

    private String normalizePhone(String phone) {
        String value = phone.trim().replace(" ", "").replace("-", "");
        if (value.matches("[6789]\\d{8}")) {
            return "+34" + value;
        }
        return value.startsWith("+") ? value : "+" + value;
    }

}
