package com.guatequeriders.api.routes.repository;

import com.guatequeriders.api.routes.model.RouteOrganizer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RouteOrganizerRepository extends JpaRepository<RouteOrganizer, Long> {

    List<RouteOrganizer> findAllByRouteId(Long routeId);

    boolean existsByUserId(Long userId);

    @Query("SELECT ro FROM RouteOrganizer ro JOIN FETCH ro.user WHERE ro.route.id = :routeId")
    List<RouteOrganizer> findAllWithUsersByRouteId(@Param("routeId") Long routeId);

    @Query("SELECT ro FROM RouteOrganizer ro JOIN FETCH ro.user WHERE ro.route.id IN :routeIds")
    List<RouteOrganizer> findAllWithUsersByRouteIdIn(@Param("routeIds") Collection<Long> routeIds);

    void deleteByRouteId(Long routeId);
}
