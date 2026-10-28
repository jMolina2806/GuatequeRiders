package com.guatequeriders.api.routes.controller;

import com.guatequeriders.api.registrations.service.RegistrationsService;
import com.guatequeriders.api.routes.dto.CreateRouteRequest;
import com.guatequeriders.api.routes.dto.RouteOrganizerResponse;
import com.guatequeriders.api.routes.dto.RouteResponse;
import com.guatequeriders.api.routes.dto.UpdateRouteRequest;
import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.model.RouteOrganizer;
import com.guatequeriders.api.routes.model.RouteStatus;
import com.guatequeriders.api.routes.photos.service.RoutePhotosService;
import com.guatequeriders.api.routes.service.RoutesService;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/routes")
public class RoutesController {

    private final RoutesService routesService;
    private final RegistrationsService registrationsService;
    private final RoutePhotosService routePhotosService;

    public RoutesController(RoutesService routesService,
                            RegistrationsService registrationsService,
                            RoutePhotosService routePhotosService) {
        this.routesService = routesService;
        this.registrationsService = registrationsService;
        this.routePhotosService = routePhotosService;
    }

    @GetMapping
    public ResponseEntity<List<RouteResponse>> getAll() {
        boolean isAdmin = isAdmin();
        List<Route> routes = routesService.findAll().stream()
                .filter(route -> isAdmin || route.getStatus() != RouteStatus.DRAFT)
                .toList();
        Map<Long, List<RouteOrganizerResponse>> organizersByRoute = groupOrganizers(
                routesService.findOrganizers(routes.stream().map(Route::getId).toList()));
        return ResponseEntity.ok(
                routes.stream()
                        .map(route -> RouteResponse.from(
                                route,
                                registrationsService.countByRouteId(route.getId()),
                                organizersByRoute.getOrDefault(route.getId(), List.of())))
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getById(@PathVariable Long id) {
        Route route = routesService.findById(id);
        if (route.getStatus() == RouteStatus.DRAFT && !isAdmin()) {
            throw new ResourceNotFoundException("Route", id);
        }
        return ResponseEntity.ok(
                RouteResponse.from(
                        route,
                        registrationsService.countByRouteId(id),
                        toOrganizerResponses(routesService.findOrganizers(id)))
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteResponse> create(@Valid @RequestBody CreateRouteRequest request) {
        Route created = routesService.create(toRoute(request), request.getOrganizerIds());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RouteResponse.from(
                        created,
                        registrationsService.countByRouteId(created.getId()),
                        toOrganizerResponses(routesService.findOrganizers(created.getId()))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody UpdateRouteRequest request) {
        Route updated = routesService.update(id, toRoute(id, request), request.getOrganizerIds());
        return ResponseEntity.ok(RouteResponse.from(
                updated,
                registrationsService.countByRouteId(id),
                toOrganizerResponses(routesService.findOrganizers(id))));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteResponse> publish(@PathVariable Long id) {
        Route published = routesService.publish(id);
        return ResponseEntity.ok(RouteResponse.from(
                published,
                registrationsService.countByRouteId(id),
                toOrganizerResponses(routesService.findOrganizers(id))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        registrationsService.deleteByRouteId(id);
        routesService.delete(id);
        routePhotosService.cleanupRouteStorage(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private Map<Long, List<RouteOrganizerResponse>> groupOrganizers(List<RouteOrganizer> organizers) {
        return organizers.stream()
                .collect(Collectors.groupingBy(
                        organizer -> organizer.getRoute().getId(),
                        Collectors.mapping(
                                organizer -> RouteOrganizerResponse.from(organizer.getUser()),
                                Collectors.toList())));
    }

    private List<RouteOrganizerResponse> toOrganizerResponses(List<RouteOrganizer> organizers) {
        return organizers.stream()
                .map(organizer -> RouteOrganizerResponse.from(organizer.getUser()))
                .toList();
    }

    private Route toRoute(CreateRouteRequest r) {
        Route route = new Route();
        route.setTitle(r.getTitle());
        route.setDescription(r.getDescription());
        route.setOrigin(r.getOrigin());
        route.setDestination(r.getDestination());
        route.setDate(r.getDate());
        route.setMeetingTime(r.getMeetingTime());
        route.setDepartureTime(r.getDepartureTime());
        route.setDistanceKm(r.getDistanceKm());
        route.setDifficulty(r.getDifficulty());
        route.setMaxParticipants(r.getMaxParticipants());
        return route;
    }

    private Route toRoute(Long id, UpdateRouteRequest r) {
        Route route = new Route();
        route.setId(id);
        route.setTitle(r.getTitle());
        route.setDescription(r.getDescription());
        route.setOrigin(r.getOrigin());
        route.setDestination(r.getDestination());
        route.setDate(r.getDate());
        route.setMeetingTime(r.getMeetingTime());
        route.setDepartureTime(r.getDepartureTime());
        route.setDistanceKm(r.getDistanceKm());
        route.setDifficulty(r.getDifficulty());
        route.setMaxParticipants(r.getMaxParticipants());
        route.setStatus(r.getStatus());
        return route;
    }
}
