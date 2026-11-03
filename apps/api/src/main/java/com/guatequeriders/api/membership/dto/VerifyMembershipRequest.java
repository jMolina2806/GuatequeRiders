package com.guatequeriders.api.membership.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyMembershipRequest(
        @NotBlank
        @Size(max = 2048)
        String token
) {
}
