package com.guatequeriders.api.notifications.service;

import com.guatequeriders.api.notifications.dto.UpdateNotificationPreferencesRequest;
import com.guatequeriders.api.notifications.model.NotificationCategory;
import com.guatequeriders.api.notifications.model.UserNotificationPreferences;
import com.guatequeriders.api.notifications.repository.UserNotificationPreferencesRepository;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationPreferencesService {

    private final UserNotificationPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;
    private final UnsubscribeTokenService unsubscribeTokenService;

    public NotificationPreferencesService(
            UserNotificationPreferencesRepository preferencesRepository,
            UserRepository userRepository,
            UnsubscribeTokenService unsubscribeTokenService) {
        this.preferencesRepository = preferencesRepository;
        this.userRepository = userRepository;
        this.unsubscribeTokenService = unsubscribeTokenService;
    }

    @Transactional
    public UserNotificationPreferences getOrCreate(User user) {
        return preferencesRepository.findById(user.getId())
                .orElseGet(() -> preferencesRepository.save(defaultPreferences(user)));
    }

    @Transactional
    public UserNotificationPreferences update(User user, UpdateNotificationPreferencesRequest request) {
        UserNotificationPreferences preferences = getOrCreate(user);
        preferences.setMerchNotifications(request.merchNotifications());
        preferences.setRouteNotifications(request.routeNotifications());
        preferences.setEventNotifications(request.eventNotifications());
        preferences.setCircuitNotifications(request.circuitNotifications());
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferences;
    }

    @Transactional(readOnly = true)
    public List<User> usersEnabledFor(NotificationCategory category) {
        List<User> users = userRepository.findAllByOrderByIdAsc();
        Map<Long, UserNotificationPreferences> preferences = preferencesRepository.findAll().stream()
                .collect(Collectors.toMap(
                        UserNotificationPreferences::getUserId,
                        Function.identity()
                ));

        return users.stream()
                .filter(user -> isEnabled(preferences.get(user.getId()), category))
                .toList();
    }

    @Transactional
    public void unsubscribe(String token, NotificationCategory category) {
        Long userId = unsubscribeTokenService.verify(token, category);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid unsubscribe token"));
        UserNotificationPreferences preferences = getOrCreate(user);
        setEnabled(preferences, category, false);
        preferences.setUpdatedAt(LocalDateTime.now());
    }

    public String tokenFor(User user, NotificationCategory category) {
        return unsubscribeTokenService.create(user.getId(), category);
    }

    private UserNotificationPreferences defaultPreferences(User user) {
        UserNotificationPreferences preferences = new UserNotificationPreferences();
        preferences.setUser(user);
        preferences.setMerchNotifications(false);
        preferences.setRouteNotifications(false);
        preferences.setEventNotifications(false);
        preferences.setCircuitNotifications(false);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferences;
    }

    private boolean isEnabled(UserNotificationPreferences preferences, NotificationCategory category) {
        if (preferences == null) {
            return false;
        }
        return switch (category) {
            case MERCH -> preferences.isMerchNotifications();
            case ROUTES -> preferences.isRouteNotifications();
            case EVENTS -> preferences.isEventNotifications();
            case CIRCUITS -> preferences.isCircuitNotifications();
        };
    }

    private void setEnabled(UserNotificationPreferences preferences,
                            NotificationCategory category,
                            boolean enabled) {
        switch (category) {
            case MERCH -> preferences.setMerchNotifications(enabled);
            case ROUTES -> preferences.setRouteNotifications(enabled);
            case EVENTS -> preferences.setEventNotifications(enabled);
            case CIRCUITS -> preferences.setCircuitNotifications(enabled);
        }
    }
}
