package com.guatequeriders.api.admin.service;

import com.guatequeriders.api.admin.dto.AdminStatsResponse;
import com.guatequeriders.api.auth.dto.UserResponse;
import com.guatequeriders.api.common.exception.ConflictException;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.eventregistrations.repository.EventRegistrationRepository;
import com.guatequeriders.api.events.model.EventStatus;
import com.guatequeriders.api.events.repository.EventRepository;
import com.guatequeriders.api.registrations.repository.RouteRegistrationRepository;
import com.guatequeriders.api.routes.model.RouteStatus;
import com.guatequeriders.api.routes.repository.RouteRepository;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final EventRepository eventRepository;
    private final RouteRegistrationRepository routeRegistrationRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    public AdminService(UserRepository userRepository,
                        RouteRepository routeRepository,
                        EventRepository eventRepository,
                        RouteRegistrationRepository routeRegistrationRepository,
                        EventRegistrationRepository eventRegistrationRepository) {
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
        this.eventRepository = eventRepository;
        this.routeRegistrationRepository = routeRegistrationRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                routeRepository.count(),
                routeRepository.countByStatus(RouteStatus.PUBLISHED),
                routeRegistrationRepository.count(),
                eventRepository.count(),
                eventRepository.countByStatus(EventStatus.PUBLISHED),
                eventRegistrationRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAllByOrderByIdAsc().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse updateRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getRole() == Role.ADMIN && newRole == Role.USER) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new ConflictException("Cannot remove the last admin");
            }
        }

        user.setRole(newRole);
        return UserResponse.from(user);
    }
}
