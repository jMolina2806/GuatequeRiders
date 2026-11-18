package com.guatequeriders.api.admin.service;

import com.guatequeriders.api.admin.dto.AdminStatsResponse;
import com.guatequeriders.api.auth.dto.UserResponse;
import com.guatequeriders.api.common.exception.ConflictException;
import com.guatequeriders.api.common.exception.DuplicateResourceException;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.eventregistrations.repository.EventRegistrationRepository;
import com.guatequeriders.api.events.model.EventStatus;
import com.guatequeriders.api.events.repository.EventRepository;
import com.guatequeriders.api.registrations.repository.RouteRegistrationRepository;
import com.guatequeriders.api.routes.model.RouteStatus;
import com.guatequeriders.api.routes.repository.RouteOrganizerRepository;
import com.guatequeriders.api.routes.repository.RouteRepository;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final MemberNumberService memberNumberService;
    private final RouteRepository routeRepository;
    private final RouteOrganizerRepository routeOrganizerRepository;
    private final EventRepository eventRepository;
    private final RouteRegistrationRepository routeRegistrationRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    public AdminService(UserRepository userRepository,
                        MemberNumberService memberNumberService,
                        RouteRepository routeRepository,
                        RouteOrganizerRepository routeOrganizerRepository,
                        EventRepository eventRepository,
                        RouteRegistrationRepository routeRegistrationRepository,
                        EventRegistrationRepository eventRegistrationRepository) {
        this.userRepository = userRepository;
        this.memberNumberService = memberNumberService;
        this.routeRepository = routeRepository;
        this.routeOrganizerRepository = routeOrganizerRepository;
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

        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new ConflictException("Cannot remove the last admin");
            }
            if (routeOrganizerRepository.existsByUserId(userId)) {
                throw new ConflictException("Cannot change role: user is organizer of one or more routes");
            }
        }

        if (newRole == Role.COLLABORATOR && user.isFounder()) {
            throw new ConflictException("Founder accounts cannot be collaborator accounts");
        }
        if (newRole == Role.COLLABORATOR && user.isMembershipActive()) {
            user.setMembershipActive(false);
            user.setMembershipVersion(user.getMembershipVersion() + 1);
        }

        if (newRole == Role.ADMIN && (!user.isMembershipActive() || user.getMemberNumber() == null)) {
            if (user.getMemberNumber() == null) {
                user.setMemberNumber(memberNumberService.forRegistrationOrder(user));
            }
            user.setMembershipActive(true);
            user.setMembershipVersion(user.getMembershipVersion() + 1);
        }

        user.setRole(newRole);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMembership(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (active && user.getRole() == Role.COLLABORATOR) {
            throw new ConflictException("Collaborator accounts cannot be members");
        }
        if (!active && user.getRole() == Role.ADMIN) {
            throw new ConflictException("Administrators must remain active members");
        }

        if (active) {
            if (user.getMemberNumber() == null) {
                user.setMemberNumber(memberNumberService.forRegistrationOrder(user));
            }
            user.setMembershipActive(true);
        } else {
            user.setMembershipActive(false);
        }

        user.setMembershipVersion(user.getMembershipVersion() + 1);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateFounder(Long userId, boolean founder) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (founder && user.getRole() == Role.COLLABORATOR) {
            throw new ConflictException("Collaborator accounts cannot be founders");
        }
        user.setFounder(founder);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMemberNumber(Long userId, String requestedNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!user.isFounder()) {
            throw new ConflictException("Manual member numbers are reserved for founders");
        }

        String digits = requestedNumber.trim().toUpperCase(Locale.ROOT);
        if (digits.startsWith("GR-")) {
            digits = digits.substring(3);
        }
        if (!digits.matches("[0-9]{1,12}")) {
            throw new IllegalArgumentException("Invalid member number format");
        }
        String normalized = "GR-" + "0".repeat(Math.max(0, 4 - digits.length())) + digits;
        if (normalized.equals(user.getMemberNumber())) {
            return UserResponse.from(user);
        }
        if (userRepository.existsByMemberNumberAndIdNot(normalized, userId)) {
            throw new DuplicateResourceException("Member number already in use");
        }

        user.setMemberNumber(normalized);
        user.setMembershipVersion(user.getMembershipVersion() + 1);

        try {
            return UserResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Member number already in use");
        }
    }

}
