package com.guatequeriders.api.admin.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateMembershipRequest(
        @NotNull Boolean active
) {
}
