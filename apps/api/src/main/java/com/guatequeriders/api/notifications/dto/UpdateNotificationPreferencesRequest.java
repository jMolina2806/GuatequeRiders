package com.guatequeriders.api.notifications.dto;

public record UpdateNotificationPreferencesRequest(
        boolean merchNotifications,
        boolean routeNotifications,
        boolean eventNotifications,
        boolean circuitNotifications
) {
}
