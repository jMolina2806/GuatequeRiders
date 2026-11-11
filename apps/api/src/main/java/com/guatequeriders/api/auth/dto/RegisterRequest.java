package com.guatequeriders.api.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^(?:[6789](?:[ -]?\\d){8}|\\+[1-9](?:[ -]?\\d){7,14})$",
                message = "Phone number must be valid"
        )
        String phone,

        @NotBlank(message = "Password is required")
        @Size(min = 10, max = 72, message = "Password must be between 10 and 72 characters")
        String password,

        @AssertTrue(message = "Privacy policy acknowledgement is required")
        boolean privacyAccepted
) {
}
