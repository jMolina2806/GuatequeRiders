package com.guatequeriders.api.notifications.dto;

import com.guatequeriders.api.notifications.model.NotificationCategory;

public record UnsubscribeResponse(
        boolean unsubscribed,
        NotificationCategory category
) {
}
