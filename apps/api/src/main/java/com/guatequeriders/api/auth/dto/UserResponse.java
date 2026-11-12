package com.guatequeriders.api.auth.dto;

import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String phone,
        Role role,
        LocalDateTime createdAt,
        boolean memberActive,
        String memberNumber,
        boolean founder
) {
    public static UserResponse from(User user) {
        boolean active = user.isMembershipActive() && user.getMemberNumber() != null;

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt(),
                active,
                user.getMemberNumber(),
                user.isFounder()
        );
    }
}
