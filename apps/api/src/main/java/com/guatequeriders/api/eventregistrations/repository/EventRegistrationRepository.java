package com.guatequeriders.api.eventregistrations.repository;

import com.guatequeriders.api.eventregistrations.model.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    long countByEventId(Long eventId);

    List<EventRegistration> findAllByEventId(Long eventId);

    @Query("SELECT e FROM EventRegistration e JOIN FETCH e.event WHERE e.user.id = :userId")
    List<EventRegistration> findAllByUserId(@Param("userId") Long userId);

    void deleteByEventId(Long eventId);
}
