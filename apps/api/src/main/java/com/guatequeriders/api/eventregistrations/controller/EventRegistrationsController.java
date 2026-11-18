package com.guatequeriders.api.eventregistrations.controller;

import com.guatequeriders.api.eventregistrations.dto.EventParticipantResponse;
import com.guatequeriders.api.eventregistrations.dto.EventRegistrationResponse;
import com.guatequeriders.api.eventregistrations.dto.EventRegistrationStatusResponse;
import com.guatequeriders.api.eventregistrations.service.EventRegistrationsService;
import com.guatequeriders.api.users.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/registrations")
public class EventRegistrationsController {

    private final EventRegistrationsService eventRegistrationsService;

    public EventRegistrationsController(EventRegistrationsService eventRegistrationsService) {
        this.eventRegistrationsService = eventRegistrationsService;
    }

    @PostMapping
    public ResponseEntity<EventRegistrationResponse> register(@PathVariable Long eventId,
                                                              @AuthenticationPrincipal User user) {
        EventRegistrationResponse created = eventRegistrationsService.register(eventId, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> unregister(@PathVariable Long eventId,
                                           @AuthenticationPrincipal User user) {
        eventRegistrationsService.unregister(eventId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventParticipantResponse>> getParticipants(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventRegistrationsService.findParticipants(eventId));
    }

    @GetMapping("/me")
    public ResponseEntity<EventRegistrationStatusResponse> getMyRegistration(@PathVariable Long eventId,
                                                                             @AuthenticationPrincipal User user) {
        boolean registered = eventRegistrationsService.isRegistered(eventId, user.getId());
        return ResponseEntity.ok(new EventRegistrationStatusResponse(registered));
    }
}
