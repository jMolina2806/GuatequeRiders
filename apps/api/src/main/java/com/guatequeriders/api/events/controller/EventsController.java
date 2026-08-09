package com.guatequeriders.api.events.controller;

import com.guatequeriders.api.eventregistrations.service.EventRegistrationsService;
import com.guatequeriders.api.events.dto.CreateEventRequest;
import com.guatequeriders.api.events.dto.EventResponse;
import com.guatequeriders.api.events.dto.UpdateEventRequest;
import com.guatequeriders.api.events.model.Event;
import com.guatequeriders.api.events.service.EventsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventsController {

    private final EventsService eventsService;
    private final EventRegistrationsService eventRegistrationsService;

    public EventsController(EventsService eventsService, EventRegistrationsService eventRegistrationsService) {
        this.eventsService = eventsService;
        this.eventRegistrationsService = eventRegistrationsService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAll() {
        return ResponseEntity.ok(
                eventsService.findAll().stream()
                        .map(event -> EventResponse.from(event, eventRegistrationsService.countByEventId(event.getId())))
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                EventResponse.from(eventsService.findById(id), eventRegistrationsService.countByEventId(id))
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        Event created = eventsService.create(toEvent(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EventResponse.from(created, eventRegistrationsService.countByEventId(created.getId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody UpdateEventRequest request) {
        Event updated = eventsService.update(id, toEvent(id, request));
        return ResponseEntity.ok(EventResponse.from(updated, eventRegistrationsService.countByEventId(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventRegistrationsService.deleteByEventId(id);
        eventsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Event toEvent(CreateEventRequest r) {
        Event event = new Event();
        event.setTitle(r.getTitle());
        event.setDescription(r.getDescription());
        event.setLocation(r.getLocation());
        event.setDate(r.getDate());
        event.setStartTime(r.getStartTime());
        event.setEndTime(r.getEndTime());
        event.setMaxParticipants(r.getMaxParticipants());
        return event;
    }

    private Event toEvent(Long id, UpdateEventRequest r) {
        Event event = new Event();
        event.setId(id);
        event.setTitle(r.getTitle());
        event.setDescription(r.getDescription());
        event.setLocation(r.getLocation());
        event.setDate(r.getDate());
        event.setStartTime(r.getStartTime());
        event.setEndTime(r.getEndTime());
        event.setMaxParticipants(r.getMaxParticipants());
        event.setStatus(r.getStatus());
        return event;
    }
}
