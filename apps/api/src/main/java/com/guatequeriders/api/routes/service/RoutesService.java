package com.guatequeriders.api.routes.service;

import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.model.RouteStatus;
import com.guatequeriders.api.routes.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoutesService {

    private final RouteRepository routeRepository;

    public RoutesService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Transactional(readOnly = true)
    public List<Route> findAll() {
        return routeRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Route findById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route", id));
    }

    @Transactional
    public Route create(Route route) {
        if (route.getStatus() == null) {
            route.setStatus(RouteStatus.DRAFT);
        }
        return routeRepository.save(route);
    }

    @Transactional
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

    @Transactional
    public void delete(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Route", id);
        }
        routeRepository.deleteById(id);
    }
}
