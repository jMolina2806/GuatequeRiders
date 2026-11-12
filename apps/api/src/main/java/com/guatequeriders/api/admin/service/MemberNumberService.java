package com.guatequeriders.api.admin.service;

import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MemberNumberService {

    private final UserRepository userRepository;

    public MemberNumberService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String forRegistrationOrder(User user) {
        if (user.getId() == null) {
            throw new IllegalStateException("User must be registered before assigning a member number");
        }

        for (long number = user.getId(); number > 0; number++) {
            String candidate = String.format(Locale.ROOT, "GR-%04d", number);
            if (!userRepository.existsByMemberNumber(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Could not allocate a unique numeric member number");
    }
}
