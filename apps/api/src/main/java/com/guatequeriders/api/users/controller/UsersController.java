package com.guatequeriders.api.users.controller;

import com.guatequeriders.api.eventregistrations.service.EventRegistrationsService;
import com.guatequeriders.api.events.dto.EventResponse;
import com.guatequeriders.api.registrations.service.RegistrationsService;
import com.guatequeriders.api.routes.dto.RouteResponse;
import com.guatequeriders.api.users.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final RegistrationsService registrationsService;
    private final EventRegistrationsService eventRegistrationsService;

    public UsersController(RegistrationsService registrationsService,
                           EventRegistrationsService eventRegistrationsService) {
        this.registrationsService = registrationsService;
        this.eventRegistrationsService = eventRegistrationsService;
    }

    @GetMapping("/me/routes")
    public ResponseEntity<List<RouteResponse>> myRoutes(@AuthenticationPrincipal User user) {
        List<RouteResponse> responses = registrationsService.findRoutesForUser(user).stream()
                .map(route -> RouteResponse.from(route, registrationsService.countByRouteId(route.getId())))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/me/events")
    public ResponseEntity<List<EventResponse>> myEvents(@AuthenticationPrincipal User user) {
        List<EventResponse> responses = eventRegistrationsService.findEventsForUser(user).stream()
                .map(event -> EventResponse.from(event, eventRegistrationsService.countByEventId(event.getId())))
                .toList();
        return ResponseEntity.ok(responses);
    }
}
