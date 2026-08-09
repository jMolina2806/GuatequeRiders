package com.guatequeriders.api.routes.service;

import com.guatequeriders.api.common.exception.ConflictException;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.model.RouteOrganizer;
import com.guatequeriders.api.routes.model.RouteStatus;
import com.guatequeriders.api.routes.repository.RouteOrganizerRepository;
import com.guatequeriders.api.routes.repository.RouteRepository;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoutesService {

    private final RouteRepository routeRepository;
    private final RouteOrganizerRepository routeOrganizerRepository;
    private final UserRepository userRepository;

    public RoutesService(RouteRepository routeRepository,
                         RouteOrganizerRepository routeOrganizerRepository,
                         UserRepository userRepository) {
        this.routeRepository = routeRepository;
        this.routeOrganizerRepository = routeOrganizerRepository;
        this.userRepository = userRepository;
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

    @Transactional(readOnly = true)
    public List<RouteOrganizer> findOrganizers(Long routeId) {
        return routeOrganizerRepository.findAllWithUsersByRouteId(routeId);
    }

    @Transactional(readOnly = true)
    public List<RouteOrganizer> findOrganizers(Collection<Long> routeIds) {
        if (routeIds.isEmpty()) {
            return List.of();
        }
        return routeOrganizerRepository.findAllWithUsersByRouteIdIn(routeIds);
    }

    @Transactional
    public Route create(Route route, List<Long> organizerIds) {
        List<User> organizers = resolveOrganizers(organizerIds);
        if (route.getStatus() == null) {
            route.setStatus(RouteStatus.DRAFT);
        }
        Route saved = routeRepository.save(route);
        for (User user : organizers) {
            routeOrganizerRepository.save(new RouteOrganizer(saved, user));
        }
        return saved;
    }

    @Transactional
    public Route update(Long id, Route updated, List<Long> organizerIds) {
        List<User> organizers = resolveOrganizers(organizerIds);
        Route existing = findById(id);

        Set<Long> requestedIds = new HashSet<>(organizers.stream().map(User::getId).toList());
        List<RouteOrganizer> currentOrganizers = routeOrganizerRepository.findAllByRouteId(id);

        List<RouteOrganizer> toRemove = currentOrganizers.stream()
                .filter(ro -> !requestedIds.contains(ro.getUser().getId()))
                .toList();
        routeOrganizerRepository.deleteAll(toRemove);

        Set<Long> currentIds = new HashSet<>(currentOrganizers.stream().map(ro -> ro.getUser().getId()).toList());
        for (User user : organizers) {
            if (!currentIds.contains(user.getId())) {
                routeOrganizerRepository.save(new RouteOrganizer(existing, user));
            }
        }

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
        routeOrganizerRepository.deleteByRouteId(id);
        routeRepository.deleteById(id);
    }

    private List<User> resolveOrganizers(List<Long> organizerIds) {
        List<Long> distinctIds = organizerIds.stream().distinct().toList();
        List<User> users = userRepository.findAllById(distinctIds);
        if (users.size() != distinctIds.size()) {
            Set<Long> foundIds = users.stream().map(User::getId).collect(Collectors.toSet());
            Long missing = distinctIds.stream().filter(id -> !foundIds.contains(id)).findFirst().orElse(null);
            throw new ResourceNotFoundException("User", missing);
        }
        for (User user : users) {
            if (user.getRole() != Role.ADMIN) {
                throw new ConflictException("Only ADMIN users can be route organizers");
            }
        }
        return users;
    }
}
