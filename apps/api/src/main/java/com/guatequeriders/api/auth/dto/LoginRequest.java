package com.guatequeriders.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username is too long")
        String username,

        @NotBlank(message = "Password is required")
        @Size(max = 64, message = "Password is too long")
        String password
) {
}
