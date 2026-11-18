package com.guatequeriders.api.notifications.dto;

import com.guatequeriders.api.notifications.model.NotificationCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UnsubscribeRequest(
        @NotBlank String token,
        @NotNull NotificationCategory category
) {
}
