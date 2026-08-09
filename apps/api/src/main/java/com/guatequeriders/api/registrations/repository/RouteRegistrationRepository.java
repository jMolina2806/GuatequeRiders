package com.guatequeriders.api.registrations.repository;

import com.guatequeriders.api.registrations.model.RouteRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRegistrationRepository extends JpaRepository<RouteRegistration, Long> {

    Optional<RouteRegistration> findByRouteIdAndUserId(Long routeId, Long userId);

    boolean existsByRouteIdAndUserId(Long routeId, Long userId);

    long countByRouteId(Long routeId);

    List<RouteRegistration> findAllByRouteId(Long routeId);

    void deleteByRouteId(Long routeId);
}
