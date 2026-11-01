package com.guatequeriders.api.auth.controller;

import com.guatequeriders.api.auth.dto.AuthResponse;
import com.guatequeriders.api.auth.dto.LoginRequest;
import com.guatequeriders.api.auth.dto.RegisterRequest;
import com.guatequeriders.api.auth.dto.UserResponse;
import com.guatequeriders.api.auth.security.JwtService;
import com.guatequeriders.api.auth.security.LoginAttemptService;
import com.guatequeriders.api.common.exception.InvalidCredentialsException;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import com.guatequeriders.api.users.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsersService usersService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    public AuthController(UsersService usersService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          LoginAttemptService loginAttemptService) {
        this.usersService = usersService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = usersService.register(request.username(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toAuthResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        loginAttemptService.assertAllowed(request.username(), clientIp);

        User user = userRepository.findByUsername(request.username())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElse(null);

        if (user == null) {
            loginAttemptService.recordFailure(request.username(), clientIp);
            throw new InvalidCredentialsException();
        }

        loginAttemptService.reset(request.username(), clientIp);
        return ResponseEntity.ok(toAuthResponse(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponse.from(user));
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, UserResponse.from(user));
    }
}
