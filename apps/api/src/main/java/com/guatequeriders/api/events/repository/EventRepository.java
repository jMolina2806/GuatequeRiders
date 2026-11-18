package com.guatequeriders.api.events.repository;

import com.guatequeriders.api.events.model.Event;
import com.guatequeriders.api.events.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByOrderByIdAsc();

    long countByStatus(EventStatus status);
}
