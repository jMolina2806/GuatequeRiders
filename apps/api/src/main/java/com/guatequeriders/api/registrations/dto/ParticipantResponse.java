package com.guatequeriders.api.registrations.dto;

import java.time.LocalDateTime;

public record ParticipantResponse(
        Long userId,
        String username,
        LocalDateTime registeredAt
) {
}
