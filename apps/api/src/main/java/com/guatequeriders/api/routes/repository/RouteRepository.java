package com.guatequeriders.api.routes.repository;

import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.model.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findAllByOrderByIdAsc();

    long countByStatus(RouteStatus status);
}
