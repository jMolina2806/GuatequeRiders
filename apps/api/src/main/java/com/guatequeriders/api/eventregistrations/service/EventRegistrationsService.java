package com.guatequeriders.api.eventregistrations.service;

import com.guatequeriders.api.common.exception.ConflictException;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.eventregistrations.dto.EventParticipantResponse;
import com.guatequeriders.api.eventregistrations.dto.EventRegistrationResponse;
import com.guatequeriders.api.eventregistrations.model.EventRegistration;
import com.guatequeriders.api.eventregistrations.repository.EventRegistrationRepository;
import com.guatequeriders.api.events.model.Event;
import com.guatequeriders.api.events.model.EventStatus;
import com.guatequeriders.api.events.service.EventsService;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import com.guatequeriders.api.notifications.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventRegistrationsService {

    private final EventRegistrationRepository registrationRepository;
    private final EventsService eventsService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public EventRegistrationsService(EventRegistrationRepository registrationRepository,
                                     EventsService eventsService,
                                     UserRepository userRepository,
                                     NotificationService notificationService) {
        this.registrationRepository = registrationRepository;
        this.eventsService = eventsService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public EventRegistrationResponse register(Long eventId, User user) {
        Event event = eventsService.findById(eventId);
        if (!isOpen(event)) {
            throw new ConflictException("Event is not open for registration");
        }
        if (registrationRepository.existsByEventIdAndUserId(eventId, user.getId())) {
            throw new ConflictException("Already registered to this event");
        }
        if (event.getMaxParticipants() != null) {
            long current = registrationRepository.countByEventId(eventId);
            if (current >= event.getMaxParticipants()) {
                throw new ConflictException("Event is full");
            }
        }

        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);
        registration.setUser(user);
        registration.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(registration);
        notificationService.notifyEventRegistration(event, user);

        return EventRegistrationResponse.from(registration);
    }

    @Transactional
    public void unregister(Long eventId, User user) {
        eventsService.findById(eventId);
        EventRegistration registration = registrationRepository
                .findByEventIdAndUserId(eventId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found for event: " + eventId));
        registrationRepository.delete(registration);
    }

    @Transactional(readOnly = true)
    public List<EventParticipantResponse> findParticipants(Long eventId) {
        eventsService.findById(eventId);
        return registrationRepository.findAllByEventId(eventId).stream()
                .map(reg -> {
                    User participant = userRepository.findById(reg.getUser().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("User", reg.getUser().getId()));
                    return new EventParticipantResponse(participant.getId(), participant.getUsername(), reg.getRegisteredAt());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isRegistered(Long eventId, Long userId) {
        return registrationRepository.existsByEventIdAndUserId(eventId, userId);
    }

    @Transactional(readOnly = true)
    public long countByEventId(Long eventId) {
        return registrationRepository.countByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public List<Event> findEventsForUser(User user) {
        return registrationRepository.findAllByUserId(user.getId()).stream()
                .map(EventRegistration::getEvent)
                .toList();
    }

    @Transactional
    public void deleteByEventId(Long eventId) {
        registrationRepository.deleteByEventId(eventId);
    }

    private boolean isOpen(Event event) {
        return event.getStatus() == EventStatus.PUBLISHED;
    }
}
