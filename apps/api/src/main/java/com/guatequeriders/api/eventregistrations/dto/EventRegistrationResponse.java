package com.guatequeriders.api.eventregistrations.dto;

import com.guatequeriders.api.eventregistrations.model.EventRegistration;

import java.time.LocalDateTime;

public record EventRegistrationResponse(
        Long eventId,
        Long userId,
        LocalDateTime registeredAt
) {
    public static EventRegistrationResponse from(EventRegistration registration) {
        return new EventRegistrationResponse(
                registration.getEvent().getId(),
                registration.getUser().getId(),
                registration.getRegisteredAt()
        );
    }
}
