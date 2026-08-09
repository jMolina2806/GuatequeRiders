package com.guatequeriders.api.events.service;

import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.events.model.Event;
import com.guatequeriders.api.events.model.EventStatus;
import com.guatequeriders.api.events.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventsService {

    private final EventRepository eventRepository;

    public EventsService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<Event> findAll() {
        return eventRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    @Transactional
    public Event create(Event event) {
        if (event.getStatus() == null) {
            event.setStatus(EventStatus.DRAFT);
        }
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(LocalDateTime.now());
        }
        return eventRepository.save(event);
    }

    @Transactional
    public Event update(Long id, Event updated) {
        Event existing = findById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setLocation(updated.getLocation());
        existing.setDate(updated.getDate());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setMaxParticipants(updated.getMaxParticipants());
        existing.setStatus(updated.getStatus());
        return existing;
    }

    @Transactional
    public void delete(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event", id);
        }
        eventRepository.deleteById(id);
    }
}
