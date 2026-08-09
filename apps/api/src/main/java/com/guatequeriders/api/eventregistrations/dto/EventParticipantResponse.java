package com.guatequeriders.api.eventregistrations.dto;

import java.time.LocalDateTime;

public record EventParticipantResponse(
        Long userId,
        String username,
        LocalDateTime registeredAt
) {
}
