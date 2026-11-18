package com.guatequeriders.api.users.controller;

import com.guatequeriders.api.eventregistrations.service.EventRegistrationsService;
import com.guatequeriders.api.events.dto.EventResponse;
import com.guatequeriders.api.registrations.service.RegistrationsService;
import com.guatequeriders.api.routes.dto.RouteOrganizerResponse;
import com.guatequeriders.api.routes.dto.RouteResponse;
import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.service.RoutesService;
import com.guatequeriders.api.users.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final RegistrationsService registrationsService;
    private final EventRegistrationsService eventRegistrationsService;
    private final RoutesService routesService;

    public UsersController(RegistrationsService registrationsService,
                           EventRegistrationsService eventRegistrationsService,
                           RoutesService routesService) {
        this.registrationsService = registrationsService;
        this.eventRegistrationsService = eventRegistrationsService;
        this.routesService = routesService;
    }

    @GetMapping("/me/routes")
    public ResponseEntity<List<RouteResponse>> myRoutes(@AuthenticationPrincipal User user) {
        List<Route> routes = registrationsService.findRoutesForUser(user);
        Map<Long, List<RouteOrganizerResponse>> organizersByRoute = routesService.findOrganizers(
                        routes.stream().map(Route::getId).toList()).stream()
                .collect(Collectors.groupingBy(
                        organizer -> organizer.getRoute().getId(),
                        Collectors.mapping(
                                organizer -> RouteOrganizerResponse.from(organizer.getUser()),
                                Collectors.toList())));
        List<RouteResponse> responses = routes.stream()
                .map(route -> RouteResponse.from(
                        route,
                        registrationsService.countByRouteId(route.getId()),
                        organizersByRoute.getOrDefault(route.getId(), List.of())))
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
