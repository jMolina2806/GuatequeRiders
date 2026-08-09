package com.guatequeriders.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        String response = mockMvc.perform(post("/api/routes")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(routeJson(title, organizerIds)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("id").asLong();
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
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", title);
        body.put("description", "Ruta de prueba");
        body.put("origin", "Madrid");
        body.put("destination", "Puerto de Navacerrada");
        body.put("date", LocalDate.now().plusDays(5).toString());
        body.put("distanceKm", 45);
        body.put("difficulty", "EASY");
        body.put("maxParticipants", 10);
        body.put("organizerIds", organizerIds);
        return objectMapper.writeValueAsString(body);
    }

    private String routeUpdateJson(String title, List<Long> organizerIds, String status) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", title);
        body.put("description", "Ruta de prueba");
        body.put("origin", "Madrid");
        body.put("destination", "Puerto de Navacerrada");
        body.put("date", LocalDate.now().plusDays(5).toString());
        body.put("distanceKm", 45);
        body.put("difficulty", "EASY");
        body.put("maxParticipants", 10);
        body.put("status", status);
        body.put("organizerIds", organizerIds);
        return objectMapper.writeValueAsString(body);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
