package com.guatequeriders.api.registrations.controller;

import com.guatequeriders.api.registrations.dto.ParticipantResponse;
import com.guatequeriders.api.registrations.dto.RegistrationResponse;
import com.guatequeriders.api.registrations.dto.RegistrationStatusResponse;
import com.guatequeriders.api.registrations.service.RegistrationsService;
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
@RequestMapping("/api/routes/{routeId}/registrations")
public class RegistrationsController {

    private final RegistrationsService registrationsService;

    public RegistrationsController(RegistrationsService registrationsService) {
        this.registrationsService = registrationsService;
    }

    @PostMapping
    public ResponseEntity<RegistrationResponse> register(@PathVariable Long routeId,
                                                         @AuthenticationPrincipal User user) {
        RegistrationResponse created = registrationsService.register(routeId, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> unregister(@PathVariable Long routeId,
                                           @AuthenticationPrincipal User user) {
        registrationsService.unregister(routeId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@PathVariable Long routeId) {
        return ResponseEntity.ok(registrationsService.findParticipants(routeId));
    }

    @GetMapping("/me")
    public ResponseEntity<RegistrationStatusResponse> getMyRegistration(@PathVariable Long routeId,
                                                                        @AuthenticationPrincipal User user) {
        boolean registered = registrationsService.isRegistered(routeId, user.getId());
        return ResponseEntity.ok(new RegistrationStatusResponse(registered));
    }
}
