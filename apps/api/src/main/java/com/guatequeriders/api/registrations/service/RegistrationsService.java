package com.guatequeriders.api.registrations.service;

import com.guatequeriders.api.common.exception.ConflictException;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.registrations.dto.ParticipantResponse;
import com.guatequeriders.api.registrations.dto.RegistrationResponse;
import com.guatequeriders.api.registrations.model.RouteRegistration;
import com.guatequeriders.api.registrations.repository.RouteRegistrationRepository;
import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.model.RouteStatus;
import com.guatequeriders.api.routes.service.RoutesService;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationsService {

    private final RouteRegistrationRepository registrationRepository;
    private final RoutesService routesService;
    private final UserRepository userRepository;

    public RegistrationsService(RouteRegistrationRepository registrationRepository,
                                RoutesService routesService,
                                UserRepository userRepository) {
        this.registrationRepository = registrationRepository;
        this.routesService = routesService;
        this.userRepository = userRepository;
    }

    @Transactional
    public RegistrationResponse register(Long routeId, User user) {
        Route route = routesService.findById(routeId);
        if (!isOpen(route)) {
            throw new ConflictException("Route is not open for registration");
        }
        if (registrationRepository.existsByRouteIdAndUserId(routeId, user.getId())) {
            throw new ConflictException("Already registered to this route");
        }
        long current = registrationRepository.countByRouteId(routeId);
        if (current >= route.getMaxParticipants()) {
            throw new ConflictException("Route is full");
        }

        RouteRegistration registration = new RouteRegistration();
        registration.setRoute(route);
        registration.setUser(user);
        registration.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(registration);

        return RegistrationResponse.from(registration);
    }

    @Transactional
    public void unregister(Long routeId, User user) {
        routesService.findById(routeId);
        RouteRegistration registration = registrationRepository
                .findByRouteIdAndUserId(routeId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found for route: " + routeId));
        registrationRepository.delete(registration);
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponse> findParticipants(Long routeId) {
        routesService.findById(routeId);
        return registrationRepository.findAllByRouteId(routeId).stream()
                .map(reg -> {
                    User participant = userRepository.findById(reg.getUser().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("User", reg.getUser().getId()));
                    return new ParticipantResponse(participant.getId(), participant.getUsername(), reg.getRegisteredAt());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isRegistered(Long routeId, Long userId) {
        return registrationRepository.existsByRouteIdAndUserId(routeId, userId);
    }

    @Transactional(readOnly = true)
    public long countByRouteId(Long routeId) {
        return registrationRepository.countByRouteId(routeId);
    }

    @Transactional(readOnly = true)
    public List<Route> findRoutesForUser(User user) {
        return registrationRepository.findAllByUserId(user.getId()).stream()
                .map(RouteRegistration::getRoute)
                .toList();
    }

    @Transactional
    public void deleteByRouteId(Long routeId) {
        registrationRepository.deleteByRouteId(routeId);
    }

    private boolean isOpen(Route route) {
        return route.getStatus() == RouteStatus.PUBLISHED || route.getStatus() == RouteStatus.IN_PROGRESS;
    }
}
