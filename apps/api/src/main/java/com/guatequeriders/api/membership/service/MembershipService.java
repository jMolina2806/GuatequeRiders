package com.guatequeriders.api.membership.service;

import com.guatequeriders.api.membership.dto.MemberCardResponse;
import com.guatequeriders.api.membership.dto.MembershipVerificationResponse;
import com.guatequeriders.api.membership.security.MembershipTokenService;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
public class MembershipService {

    private final UserRepository userRepository;
    private final MembershipTokenService tokenService;

    public MembershipService(
            UserRepository userRepository,
            MembershipTokenService tokenService
    ) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public MemberCardResponse getCard(User user) {
        User current = userRepository.findById(user.getId())
                .orElseThrow(() -> new AccessDeniedException("Membership unavailable"));

        ensureActive(current);

        MembershipTokenService.IssuedVerification verification =
                tokenService.issue(current.getId(), current.getMembershipVersion());

        return new MemberCardResponse(
                current.getMemberNumber(),
                current.getUsername(),
                verification.code(),
                verification.expiresAt()
        );
    }

    @Transactional(readOnly = true)
    public MembershipVerificationResponse verify(String token) {
        MembershipTokenService.VerificationClaims claims = tokenService.parse(token);
        User user = userRepository.findById(claims.userId())
                .orElseThrow(this::unavailable);

        if (!isActive(user) || user.getMembershipVersion() != claims.membershipVersion()) {
            throw unavailable();
        }

        return new MembershipVerificationResponse(
                true,
                user.getMemberNumber(),
                user.getUsername(),
                claims.code(),
                claims.expiresAt()
        );
    }

    private void ensureActive(User user) {
        if (!isActive(user)) {
            throw new AccessDeniedException("Active membership required");
        }
    }

    private boolean isActive(User user) {
        return user.isMembershipActive() && user.getMemberNumber() != null;
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification unavailable");
    }
}
