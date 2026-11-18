package com.guatequeriders.api.auth.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
