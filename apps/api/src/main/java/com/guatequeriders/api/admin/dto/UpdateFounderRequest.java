package com.guatequeriders.api.admin.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateFounderRequest(
        @NotNull Boolean founder
) {
}
