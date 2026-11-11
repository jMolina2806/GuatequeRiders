package com.guatequeriders.api.membership.dto;

import java.time.Instant;

public record MembershipVerificationResponse(
        boolean valid,
        String memberNumber,
        String username,
        String verificationCode,
        Instant expiresAt
) {
}
