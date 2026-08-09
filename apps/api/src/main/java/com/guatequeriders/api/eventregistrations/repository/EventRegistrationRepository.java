package com.guatequeriders.api.eventregistrations.repository;

import com.guatequeriders.api.eventregistrations.model.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    long countByEventId(Long eventId);

    List<EventRegistration> findAllByEventId(Long eventId);

    void deleteByEventId(Long eventId);
}
