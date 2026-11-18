package com.guatequeriders.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guatequeriders.api.routes.service.RoutesService;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:routeorg;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class RouteOrganizerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoutesService routesService;

    @Test
    void createRouteWithUserOrganizerIsRejectedWith409() throws Exception {
        Long demoId = userRepository.findByUsername("demo").orElseThrow().getId();

        mockMvc.perform(post("/api/routes")
                        .header("Authorization", bearer(loginAsAdmin()))
                        .contentType(APPLICATION_JSON)
                        .content(routeJson("Ruta con USER", List.of(demoId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only ADMIN users can be route organizers"));
    }

    @Test
    void createRouteWithoutOrganizerIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/routes")
                        .header("Authorization", bearer(loginAsAdmin()))
                        .contentType(APPLICATION_JSON)
                        .content(routeJson("Ruta sin organizador", List.of())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRouteWithAdminOrganizerSucceeds() throws Exception {
        Long adminId = userRepository.findByUsername("admin").orElseThrow().getId();

        mockMvc.perform(post("/api/routes")
                        .header("Authorization", bearer(loginAsAdmin()))
                        .contentType(APPLICATION_JSON)
                        .content(routeJson("Ruta con ADMIN", List.of(adminId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organizers[0].id").value(adminId));
    }

    @Test
    void adminOrganizerCannotBeDemotedUntilRemovedFromAllRoutes() throws Exception {
        User admin2 = createUser("admin2", Role.ADMIN);
        Long admin1Id = userRepository.findByUsername("admin").orElseThrow().getId();
        String token = loginAsAdmin();

        long routeId = createRoute(token, "Ruta de admin2", List.of(admin2.getId()));

        mockMvc.perform(patch("/api/admin/users/{id}/role", admin2.getId())
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "USER"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Cannot change role: user is organizer of one or more routes"));

        mockMvc.perform(put("/api/routes/{id}", routeId)
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(routeUpdateJson("Ruta de admin2", List.of(admin1Id), "DRAFT")))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/users/{id}/role", admin2.getId())
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getRoutesNeverReturnsAUserAsOrganizer() throws Exception {
        String response = mockMvc.perform(get("/api/routes"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode routes = objectMapper.readTree(response);
        assertThat(routes).isNotEmpty();

        for (JsonNode route : routes) {
            JsonNode organizers = route.get("organizers");
            assertThat(organizers).isNotEmpty();
            for (JsonNode organizer : organizers) {
                String username = organizer.get("username").asText();
                User user = userRepository.findByUsername(username).orElseThrow();
                assertThat(user.getRole()).isEqualTo(Role.ADMIN);
            }
        }
    }

    @Test
    void publishDraftRouteSucceeds() throws Exception {
        Long adminId = userRepository.findByUsername("admin").orElseThrow().getId();
        String token = loginAsAdmin();
        long routeId = createRoute(token, "Ruta a publicar", List.of(adminId));

        mockMvc.perform(patch("/api/routes/{id}/publish", routeId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void publishNonDraftRouteIsRejectedWith409() throws Exception {
        Long adminId = userRepository.findByUsername("admin").orElseThrow().getId();
        String token = loginAsAdmin();
        long routeId = createRoute(token, "Ruta ya publicada", List.of(adminId));

        mockMvc.perform(patch("/api/routes/{id}/publish", routeId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/routes/{id}/publish", routeId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only DRAFT routes can be published"));
    }

    @Test
    void draftRouteIsHiddenFromPublicAndReturns404ForNonAdmin() throws Exception {
        Long adminId = userRepository.findByUsername("admin").orElseThrow().getId();
        String token = loginAsAdmin();
        long routeId = createRoute(token, "Ruta oculta", List.of(adminId));

        mockMvc.perform(get("/api/routes/{id}", routeId))
                .andExpect(status().isNotFound());

        String list = mockMvc.perform(get("/api/routes"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode routes = objectMapper.readTree(list);
        for (JsonNode route : routes) {
            assertThat(route.get("id").asLong()).isNotEqualTo(routeId);
        }

        mockMvc.perform(get("/api/routes/{id}", routeId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(routeId));
    }

    @Test
    void autoCompleteMarksPastRoutesAndSkipsDraftAndFuture() throws Exception {
        Long adminId = userRepository.findByUsername("admin").orElseThrow().getId();
        String token = loginAsAdmin();

        long pastPublished = createRoute(token, "Ruta pasada", List.of(adminId),
                LocalDate.now().minusDays(2).toString());
        updateStatus(token, pastPublished, "PUBLISHED", LocalDate.now().minusDays(2).toString());
        long pastDraft = createRoute(token, "Borrador pasado", List.of(adminId),
                LocalDate.now().minusDays(2).toString());
        long futurePublished = createRoute(token, "Ruta futura", List.of(adminId),
                LocalDate.now().plusDays(3).toString());
        updateStatus(token, futurePublished, "PUBLISHED", LocalDate.now().plusDays(3).toString());

        int completed = routesService.completePastRoutes();
        assertThat(completed).isGreaterThanOrEqualTo(1);

        String pastResponse = mockMvc.perform(get("/api/routes/{id}", pastPublished)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertThat(objectMapper.readTree(pastResponse).get("status").asText()).isEqualTo("COMPLETED");

        String draftResponse = mockMvc.perform(get("/api/routes/{id}", pastDraft)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertThat(objectMapper.readTree(draftResponse).get("status").asText()).isEqualTo("DRAFT");

        String futureResponse = mockMvc.perform(get("/api/routes/{id}", futurePublished)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertThat(objectMapper.readTree(futureResponse).get("status").asText()).isEqualTo("PUBLISHED");
    }

    private String loginAsAdmin() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", "admin",
                "password", "admin123"));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("token").asText();
    }

    private long createRoute(String token, String title, List<Long> organizerIds) throws Exception {
        return createRoute(token, title, organizerIds, LocalDate.now().plusDays(5).toString());
    }

    private long createRoute(String token, String title, List<Long> organizerIds, String date) throws Exception {
        String response = mockMvc.perform(post("/api/routes")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(routeJson(title, organizerIds, date)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void updateStatus(String token, long routeId, String status, String date) throws Exception {
        mockMvc.perform(put("/api/routes/{id}", routeId)
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(routeUpdateJson("Ruta", List.of(
                                userRepository.findByUsername("admin").orElseThrow().getId()), status, date)))
                .andExpect(status().isOk());
    }

    private User createUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@guatequeriders.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String routeJson(String title, List<Long> organizerIds) throws Exception {
        return routeJson(title, organizerIds, LocalDate.now().plusDays(5).toString());
    }

    private String routeJson(String title, List<Long> organizerIds, String date) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", title);
        body.put("description", "Ruta de prueba");
        body.put("origin", "Madrid");
        body.put("destination", "Puerto de Navacerrada");
        body.put("date", date);
        body.put("distanceKm", 45);
        body.put("difficulty", "EASY");
        body.put("maxParticipants", 10);
        body.put("organizerIds", organizerIds);
        body.put("meetingTime", "08:00");
        body.put("departureTime", "08:30");
        return objectMapper.writeValueAsString(body);
    }

    private String routeUpdateJson(String title, List<Long> organizerIds, String status) throws Exception {
        return routeUpdateJson(title, organizerIds, status, LocalDate.now().plusDays(5).toString());
    }

    private String routeUpdateJson(String title, List<Long> organizerIds, String status, String date) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", title);
        body.put("description", "Ruta de prueba");
        body.put("origin", "Madrid");
        body.put("destination", "Puerto de Navacerrada");
        body.put("date", date);
        body.put("distanceKm", 45);
        body.put("difficulty", "EASY");
        body.put("maxParticipants", 10);
        body.put("status", status);
        body.put("organizerIds", organizerIds);
        body.put("meetingTime", "08:00");
        body.put("departureTime", "08:30");
        return objectMapper.writeValueAsString(body);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
