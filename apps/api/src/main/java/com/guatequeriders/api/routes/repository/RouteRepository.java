package com.guatequeriders.api.routes.repository;

import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.model.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findAllByOrderByIdAsc();

    List<Route> findAllByStatusInAndDateBefore(Collection<RouteStatus> statuses, LocalDate date);

    long countByStatus(RouteStatus status);
}
