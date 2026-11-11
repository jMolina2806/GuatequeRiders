package com.guatequeriders.api.users.service;

import com.guatequeriders.api.common.exception.DuplicateResourceException;
import com.guatequeriders.api.legal.LegalVersions;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class UsersService {

    private static final char[] MEMBER_NUMBER_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

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
        user.setMemberNumber(generateMemberNumber());
        user.setMembershipActive(true);
        user.setMembershipValidUntil(LocalDate.now().plusYears(1));
        user.setMembershipVersion(1);

        return userRepository.save(user);
    }

    private String normalizePhone(String phone) {
        String value = phone.trim().replace(" ", "").replace("-", "");
        if (value.matches("[6789]\\d{8}")) {
            return "+34" + value;
        }
        return value.startsWith("+") ? value : "+" + value;
    }

    private String generateMemberNumber() {
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder value = new StringBuilder("GR-");
            for (int i = 0; i < 8; i++) {
                value.append(MEMBER_NUMBER_ALPHABET[secureRandom.nextInt(MEMBER_NUMBER_ALPHABET.length)]);
            }
            String candidate = value.toString();
            if (!userRepository.existsByMemberNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique member number");
    }
}
