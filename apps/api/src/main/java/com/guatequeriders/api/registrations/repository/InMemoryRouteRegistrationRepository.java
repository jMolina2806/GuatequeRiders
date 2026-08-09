package com.guatequeriders.api.registrations.repository;

import com.guatequeriders.api.registrations.model.RouteRegistration;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryRouteRegistrationRepository implements RouteRegistrationRepository {

    private final List<RouteRegistration> registrations = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public RouteRegistration save(RouteRegistration registration) {
        if (registration.getId() == null) {
            registration.setId(idCounter.getAndIncrement());
        }
        registrations.removeIf(r -> r.getId().equals(registration.getId()));
        registrations.add(registration);
        return registration;
    }

    @Override
    public Optional<RouteRegistration> findByRouteIdAndUserId(Long routeId, Long userId) {
        return registrations.stream()
                .filter(r -> r.getRouteId().equals(routeId) && r.getUserId().equals(userId))
                .findFirst();
    }

    @Override
    public boolean existsByRouteIdAndUserId(Long routeId, Long userId) {
        return findByRouteIdAndUserId(routeId, userId).isPresent();
    }

    @Override
    public long countByRouteId(Long routeId) {
        return registrations.stream()
                .filter(r -> r.getRouteId().equals(routeId))
                .count();
    }

    @Override
    public List<RouteRegistration> findAllByRouteId(Long routeId) {
        return new ArrayList<>(registrations.stream()
                .filter(r -> r.getRouteId().equals(routeId))
                .toList());
    }

    @Override
    public void delete(RouteRegistration registration) {
        registrations.remove(registration);
    }
}
