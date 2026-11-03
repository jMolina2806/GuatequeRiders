package com.guatequeriders.api.membership.dto;

import java.time.Instant;
import java.time.LocalDate;

public record MemberCardResponse(
        String memberNumber,
        String username,
        LocalDate validUntil,
        String verificationCode,
        Instant verificationExpiresAt,
        String qrDataUrl
) {
}
