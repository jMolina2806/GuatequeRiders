package com.guatequeriders.api.admin.dto;

import com.guatequeriders.api.users.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull(message = "Role is required")
        Role role
) {
}
