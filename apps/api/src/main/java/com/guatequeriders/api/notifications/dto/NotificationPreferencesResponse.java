package com.guatequeriders.api.notifications.dto;

import com.guatequeriders.api.notifications.model.UserNotificationPreferences;

public record NotificationPreferencesResponse(
        boolean merchNotifications,
        boolean routeNotifications,
        boolean eventNotifications,
        boolean circuitNotifications
) {
    public static NotificationPreferencesResponse from(UserNotificationPreferences preferences) {
        return new NotificationPreferencesResponse(
                preferences.isMerchNotifications(),
                preferences.isRouteNotifications(),
                preferences.isEventNotifications(),
                preferences.isCircuitNotifications()
        );
    }
}
