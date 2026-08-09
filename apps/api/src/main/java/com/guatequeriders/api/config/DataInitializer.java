package com.guatequeriders.api.config;

import com.guatequeriders.api.registrations.model.RouteRegistration;
import com.guatequeriders.api.registrations.repository.RouteRegistrationRepository;
import com.guatequeriders.api.routes.model.Difficulty;
import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.model.RouteStatus;
import com.guatequeriders.api.routes.repository.RouteRepository;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final RouteRegistrationRepository registrationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RouteRepository routeRepository,
                           RouteRegistrationRepository registrationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
        this.registrationRepository = registrationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedRoutes();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@guatequeriders.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("demo")) {
            User demo = new User();
            demo.setUsername("demo");
            demo.setEmail("demo@guatequeriders.com");
            demo.setPassword(passwordEncoder.encode("demo123"));
            demo.setRole(Role.USER);
            demo.setCreatedAt(LocalDateTime.now());
            userRepository.save(demo);
        }
    }

    private void seedRoutes() {
        if (routeRepository.count() > 0) {
            return;
        }

        Route rutaDelValle = routeRepository.save(new Route(
                "Ruta del Valle",
                "Recorrido escénico por los valles del sur con paradas en miradores naturales.",
                "Antigua Guatemala",
                "San Miguel Dueñas",
                LocalDate.now().plusDays(7),
                45.0,
                Difficulty.EASY,
                20,
                RouteStatus.PUBLISHED
        ));

        routeRepository.save(new Route(
                "Ascenso a la Montaña",
                "Desafío de montaña con curvas cerradas y paisajes impresionantes.",
                "Guatemala City",
                "El Cerrito",
                LocalDate.now().plusDays(14),
                120.0,
                Difficulty.HARD,
                15,
                RouteStatus.PUBLISHED
        ));

        routeRepository.save(new Route(
                "Costa al Atardecer",
                "Ruta costera ideal para disfrutar del atardecer junto al mar.",
                "Escuintla",
                "Monterrico",
                LocalDate.now().plusDays(21),
                85.0,
                Difficulty.MODERATE,
                25,
                RouteStatus.DRAFT
        ));

        userRepository.findByUsername("demo").ifPresent(demo -> {
            RouteRegistration registration = new RouteRegistration();
            registration.setRoute(rutaDelValle);
            registration.setUser(demo);
            registration.setRegisteredAt(LocalDateTime.now());
            registrationRepository.save(registration);
        });
    }
}
