package com.guatequeriders.api.routes.controller;

import com.guatequeriders.api.registrations.service.RegistrationsService;
import com.guatequeriders.api.routes.dto.CreateRouteRequest;
import com.guatequeriders.api.routes.dto.RouteResponse;
import com.guatequeriders.api.routes.dto.UpdateRouteRequest;
import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.service.RoutesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RoutesController {

    private final RoutesService routesService;
    private final RegistrationsService registrationsService;

    public RoutesController(RoutesService routesService, RegistrationsService registrationsService) {
        this.routesService = routesService;
        this.registrationsService = registrationsService;
    }

    @GetMapping
    public ResponseEntity<List<RouteResponse>> getAll() {
        return ResponseEntity.ok(
                routesService.findAll().stream()
                        .map(RouteResponse::from)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                RouteResponse.from(routesService.findById(id))
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteResponse> create(@Valid @RequestBody CreateRouteRequest request) {
        Route created = routesService.create(toRoute(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RouteResponse.from(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody UpdateRouteRequest request) {
        Route updated = routesService.update(id, toRoute(id, request));
        return ResponseEntity.ok(RouteResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        registrationsService.deleteByRouteId(id);
        routesService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Route toRoute(CreateRouteRequest r) {
        Route route = new Route();
        route.setTitle(r.getTitle());
        route.setDescription(r.getDescription());
        route.setOrigin(r.getOrigin());
        route.setDestination(r.getDestination());
        route.setDate(r.getDate());
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
        route.setDistanceKm(r.getDistanceKm());
        route.setDifficulty(r.getDifficulty());
        route.setMaxParticipants(r.getMaxParticipants());
        route.setStatus(r.getStatus());
        return route;
    }
}
