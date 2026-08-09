package com.guatequeriders.api.registrations.dto;

import com.guatequeriders.api.registrations.model.RouteRegistration;

import java.time.LocalDateTime;

public record RegistrationResponse(
        Long routeId,
        Long userId,
        LocalDateTime registeredAt
) {
    public static RegistrationResponse from(RouteRegistration registration) {
        return new RegistrationResponse(
                registration.getRoute().getId(),
                registration.getUser().getId(),
                registration.getRegisteredAt()
        );
    }
}
