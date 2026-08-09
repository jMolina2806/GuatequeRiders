package com.guatequeriders.api.routes.service;

import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.routes.model.Difficulty;
import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.model.RouteStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RoutesService {

    private final List<Route> routes = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @PostConstruct
    public void init() {
        routes.add(new Route(
                idCounter.getAndIncrement(),
                "Ruta del Valle",
                "Recorrido escénico por los valles del sur con paradas en miradores naturales.",
                "Antigua Guatemala",
                "San Miguel Dueñas",
                LocalDate.now().plusDays(7),
                45.0,
                Difficulty.EASY,
                20,
                5,
                RouteStatus.PUBLISHED
        ));

        routes.add(new Route(
                idCounter.getAndIncrement(),
                "Ascenso a la Montaña",
                "Desafío de montaña con curvas cerradas y paisajes impresionantes.",
                "Guatemala City",
                "El Cerrito",
                LocalDate.now().plusDays(14),
                120.0,
                Difficulty.HARD,
                15,
                12,
                RouteStatus.PUBLISHED
        ));

        routes.add(new Route(
                idCounter.getAndIncrement(),
                "Costa al Atardecer",
                "Ruta costera ideal para disfrutar del atardecer junto al mar.",
                "Escuintla",
                "Monterrico",
                LocalDate.now().plusDays(21),
                85.0,
                Difficulty.MODERATE,
                25,
                8,
                RouteStatus.DRAFT
        ));
    }

    public List<Route> findAll() {
        return new ArrayList<>(routes);
    }

    public Route findById(Long id) {
        return routes.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Route", id));
    }

    public Route create(Route route) {
        route.setId(idCounter.getAndIncrement());
        route.setCurrentParticipants(0);
        if (route.getStatus() == null) {
            route.setStatus(RouteStatus.DRAFT);
        }
        routes.add(route);
        return route;
    }

    public Route update(Long id, Route updated) {
        Route existing = findById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setOrigin(updated.getOrigin());
        existing.setDestination(updated.getDestination());
        existing.setDate(updated.getDate());
        existing.setDistanceKm(updated.getDistanceKm());
        existing.setDifficulty(updated.getDifficulty());
        existing.setMaxParticipants(updated.getMaxParticipants());
        existing.setStatus(updated.getStatus());
        return existing;
    }

    public void delete(Long id) {
        Route route = findById(id);
        routes.remove(route);
    }
}
