package com.guatequeriders.api.circuits.repository;

import com.guatequeriders.api.circuits.model.CircuitActivity;
import com.guatequeriders.api.circuits.model.CircuitActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CircuitActivityRepository extends JpaRepository<CircuitActivity, Long> {

    List<CircuitActivity> findAllByOrderByDateAscIdAsc();

    List<CircuitActivity> findAllByStatusAndDateBefore(CircuitActivityStatus status, LocalDate date);
}
