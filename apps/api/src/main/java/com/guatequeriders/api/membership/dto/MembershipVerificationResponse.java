package com.guatequeriders.api.membership.dto;

import java.time.Instant;
import java.time.LocalDate;

public record MembershipVerificationResponse(
        boolean valid,
        String memberNumber,
        String username,
        LocalDate validUntil,
        String verificationCode,
        Instant expiresAt
) {
}
