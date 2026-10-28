package com.guatequeriders.api.routes.photos.repository;

import com.guatequeriders.api.routes.photos.model.RoutePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoutePhotoRepository extends JpaRepository<RoutePhoto, Long> {

    List<RoutePhoto> findAllByRouteIdOrderByCreatedAtAscIdAsc(Long routeId);

    Optional<RoutePhoto> findByIdAndRouteId(Long id, Long routeId);

    long countByRouteIdAndUserId(Long routeId, Long userId);
}
