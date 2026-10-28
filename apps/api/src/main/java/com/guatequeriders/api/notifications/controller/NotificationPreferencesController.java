package com.guatequeriders.api.notifications.controller;

import com.guatequeriders.api.notifications.dto.NotificationPreferencesResponse;
import com.guatequeriders.api.notifications.dto.UnsubscribeRequest;
import com.guatequeriders.api.notifications.dto.UnsubscribeResponse;
import com.guatequeriders.api.notifications.dto.UpdateNotificationPreferencesRequest;
import com.guatequeriders.api.notifications.service.NotificationPreferencesService;
import com.guatequeriders.api.users.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationPreferencesController {

    private final NotificationPreferencesService preferencesService;

    public NotificationPreferencesController(NotificationPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferencesResponse> getPreferences(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                NotificationPreferencesResponse.from(preferencesService.getOrCreate(user))
        );
    }

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateNotificationPreferencesRequest request) {
        return ResponseEntity.ok(
                NotificationPreferencesResponse.from(preferencesService.update(user, request))
        );
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<UnsubscribeResponse> unsubscribe(
            @Valid @RequestBody UnsubscribeRequest request) {
        preferencesService.unsubscribe(request.token(), request.category());
        return ResponseEntity.ok(new UnsubscribeResponse(true, request.category()));
    }
}
