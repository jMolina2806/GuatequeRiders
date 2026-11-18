package com.guatequeriders.api.routes.dto;

import com.guatequeriders.api.users.model.User;

public record RouteOrganizerResponse(
        Long id,
        String username
) {
    public static RouteOrganizerResponse from(User user) {
        return new RouteOrganizerResponse(user.getId(), user.getUsername());
    }
}
