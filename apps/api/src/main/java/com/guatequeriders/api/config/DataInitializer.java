package com.guatequeriders.api.config;

import com.guatequeriders.api.eventregistrations.model.EventRegistration;
import com.guatequeriders.api.eventregistrations.repository.EventRegistrationRepository;
import com.guatequeriders.api.events.model.Event;
import com.guatequeriders.api.events.model.EventStatus;
import com.guatequeriders.api.events.repository.EventRepository;
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
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final RouteRegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RouteRepository routeRepository,
                           RouteRegistrationRepository registrationRepository,
                           EventRepository eventRepository,
                           EventRegistrationRepository eventRegistrationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedRoutes();
        seedEvents();
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

    private void seedEvents() {
        if (eventRepository.count() > 0) {
            return;
        }

        Event nocheDePelicula = new Event(
                "Noche de Película",
                "Proyección de moto-documentales en pantalla grande con palomitas y buen ambiente.",
                "Salón del Club",
                LocalDate.now().plusDays(3),
                LocalTime.of(19, 0),
                LocalTime.of(22, 0),
                30,
                EventStatus.PUBLISHED
        );
        nocheDePelicula.setCreatedAt(LocalDateTime.now());
        eventRepository.save(nocheDePelicula);

        Event torneoDeBolos = new Event(
                "Torneo de Bolos",
                "Torneo amistoso de bolos entre miembros del club. Sin plazas limitadas.",
                "Bolera Central",
                LocalDate.now().plusDays(10),
                LocalTime.of(18, 30),
                null,
                null,
                EventStatus.PUBLISHED
        );
        torneoDeBolos.setCreatedAt(LocalDateTime.now());
        eventRepository.save(torneoDeBolos);

        Event picnicEnElLago = new Event(
                "Picnic en el Lago",
                "Quedada familiar junto al lago con juegos y convivencia.",
                "Lago de Amatitlán",
                LocalDate.now().plusDays(25),
                LocalTime.of(10, 0),
                LocalTime.of(15, 0),
                40,
                EventStatus.DRAFT
        );
        picnicEnElLago.setCreatedAt(LocalDateTime.now());
        eventRepository.save(picnicEnElLago);

        userRepository.findByUsername("demo").ifPresent(demo -> {
            EventRegistration registration = new EventRegistration();
            registration.setEvent(nocheDePelicula);
            registration.setUser(demo);
            registration.setRegisteredAt(LocalDateTime.now());
            eventRegistrationRepository.save(registration);
        });
    }
}
