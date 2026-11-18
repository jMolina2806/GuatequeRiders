package com.guatequeriders.api.membership.dto;

import java.time.Instant;

public record MemberCardResponse(
        String memberNumber,
        String username,
        String verificationCode,
        Instant verificationExpiresAt
) {
}
