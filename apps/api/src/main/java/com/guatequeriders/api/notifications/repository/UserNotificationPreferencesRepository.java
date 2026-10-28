package com.guatequeriders.api.notifications.repository;

import com.guatequeriders.api.notifications.model.UserNotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationPreferencesRepository
        extends JpaRepository<UserNotificationPreferences, Long> {
}
