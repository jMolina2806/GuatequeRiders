package com.guatequeriders.api.routes.service;

import com.guatequeriders.api.common.exception.ConflictException;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.model.RouteOrganizer;
import com.guatequeriders.api.routes.model.RouteStatus;
import com.guatequeriders.api.routes.repository.RouteOrganizerRepository;
import com.guatequeriders.api.routes.repository.RouteRepository;
import com.guatequeriders.api.notifications.service.NotificationService;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoutesService {

    private final RouteRepository routeRepository;
    private final RouteOrganizerRepository routeOrganizerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public RoutesService(RouteRepository routeRepository,
                         RouteOrganizerRepository routeOrganizerRepository,
                         UserRepository userRepository,
                         NotificationService notificationService) {
        this.routeRepository = routeRepository;
        this.routeOrganizerRepository = routeOrganizerRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<Route> findAll() {
        return routeRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Route findById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route", id));
    }

    @Transactional(readOnly = true)
    public List<RouteOrganizer> findOrganizers(Long routeId) {
        return routeOrganizerRepository.findAllWithUsersByRouteId(routeId);
    }

    @Transactional(readOnly = true)
    public List<RouteOrganizer> findOrganizers(Collection<Long> routeIds) {
        if (routeIds.isEmpty()) {
            return List.of();
        }
        return routeOrganizerRepository.findAllWithUsersByRouteIdIn(routeIds);
    }

    @Transactional
    public Route create(Route route, List<Long> organizerIds) {
        validateTimes(route.getMeetingTime(), route.getDepartureTime());
        List<User> organizers = resolveOrganizers(organizerIds);
        if (route.getStatus() == null) {
            route.setStatus(RouteStatus.DRAFT);
        }
        Route saved = routeRepository.save(route);
        for (User user : organizers) {
            routeOrganizerRepository.save(new RouteOrganizer(saved, user));
        }
        return saved;
    }

    @Transactional
    public Route update(Long id, Route updated, List<Long> organizerIds) {
        validateTimes(updated.getMeetingTime(), updated.getDepartureTime());
        List<User> organizers = resolveOrganizers(organizerIds);
        Route existing = findById(id);
        RouteStatus previousStatus = existing.getStatus();

        Set<Long> requestedIds = new HashSet<>(organizers.stream().map(User::getId).toList());
        List<RouteOrganizer> currentOrganizers = routeOrganizerRepository.findAllByRouteId(id);

        List<RouteOrganizer> toRemove = currentOrganizers.stream()
                .filter(ro -> !requestedIds.contains(ro.getUser().getId()))
                .toList();
        routeOrganizerRepository.deleteAll(toRemove);

        Set<Long> currentIds = new HashSet<>(currentOrganizers.stream().map(ro -> ro.getUser().getId()).toList());
        for (User user : organizers) {
            if (!currentIds.contains(user.getId())) {
                routeOrganizerRepository.save(new RouteOrganizer(existing, user));
            }
        }

        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setOrigin(updated.getOrigin());
        existing.setDestination(updated.getDestination());
        existing.setDate(updated.getDate());
        existing.setMeetingTime(updated.getMeetingTime());
        existing.setDepartureTime(updated.getDepartureTime());
        existing.setDistanceKm(updated.getDistanceKm());
        existing.setDifficulty(updated.getDifficulty());
        existing.setMaxParticipants(updated.getMaxParticipants());
        existing.setStatus(updated.getStatus());

        if (previousStatus != RouteStatus.PUBLISHED && existing.getStatus() == RouteStatus.PUBLISHED) {
            notificationService.notifyRoutePublished(existing);
        } else if (previousStatus == RouteStatus.PUBLISHED && existing.getStatus() == RouteStatus.CANCELLED) {
            notificationService.notifyRouteCancelled(existing);
        } else if (previousStatus == RouteStatus.PUBLISHED && existing.getStatus() == RouteStatus.PUBLISHED) {
            notificationService.notifyRouteUpdated(existing);
        }
        return existing;
    }

    @Transactional
    public Route publish(Long id) {
        Route route = findById(id);
        if (route.getStatus() != RouteStatus.DRAFT) {
            throw new ConflictException("Only DRAFT routes can be published");
        }
        if (route.getDate() == null || route.getDate().isBefore(LocalDate.now())) {
            throw new ConflictException("Cannot publish a route with a past date");
        }
        if (route.getMeetingTime() == null || route.getDepartureTime() == null) {
            throw new ConflictException("Meeting and departure times are required before publishing");
        }
        if (routeOrganizerRepository.findAllByRouteId(id).isEmpty()) {
            throw new ConflictException("At least one organizer is required before publishing");
        }
        route.setStatus(RouteStatus.PUBLISHED);
        notificationService.notifyRoutePublished(route);
        return route;
    }

    @Transactional
    public int completePastRoutes() {
        List<Route> pastActive = routeRepository.findAllByStatusInAndDateBefore(
                List.of(RouteStatus.PUBLISHED, RouteStatus.IN_PROGRESS), LocalDate.now());
        for (Route route : pastActive) {
            route.setStatus(RouteStatus.COMPLETED);
        }
        return pastActive.size();
    }

    @Transactional
    public void delete(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Route", id);
        }
        routeOrganizerRepository.deleteByRouteId(id);
        routeRepository.deleteById(id);
    }

    private List<User> resolveOrganizers(List<Long> organizerIds) {
        List<Long> distinctIds = organizerIds.stream().distinct().toList();
        List<User> users = userRepository.findAllById(distinctIds);
        if (users.size() != distinctIds.size()) {
            Set<Long> foundIds = users.stream().map(User::getId).collect(Collectors.toSet());
            Long missing = distinctIds.stream().filter(id -> !foundIds.contains(id)).findFirst().orElse(null);
            throw new ResourceNotFoundException("User", missing);
        }
        for (User user : users) {
            if (user.getRole() != Role.ADMIN) {
                throw new ConflictException("Only ADMIN users can be route organizers");
            }
        }
        return users;
    }

    private void validateTimes(LocalTime meetingTime, LocalTime departureTime) {
        if (meetingTime == null || departureTime == null) {
            throw new IllegalArgumentException("Meeting and departure times are required");
        }
        if (!departureTime.isAfter(meetingTime)) {
            throw new IllegalArgumentException("Departure time must be after meeting time");
        }
    }
}
