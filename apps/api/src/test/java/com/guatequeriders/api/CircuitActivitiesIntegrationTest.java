package com.guatequeriders.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guatequeriders.api.circuits.model.CircuitActivity;
import com.guatequeriders.api.circuits.model.CircuitActivityStatus;
import com.guatequeriders.api.circuits.repository.CircuitActivityRepository;
import com.guatequeriders.api.circuits.service.CircuitActivitiesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:circuitactivities;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class CircuitActivitiesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CircuitActivityRepository circuitActivityRepository;

    @Autowired
    private CircuitActivitiesService circuitActivitiesService;

    @Test
    void publicUsersOnlySeePublishedActivities() throws Exception {
        String token = loginAsAdmin();
        long draftId = createActivity(token, "Circuito en borrador", LocalDate.now().plusDays(5));

        mockMvc.perform(get("/api/circuits"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                    assertThat(body.findValuesAsText("id")).doesNotContain(String.valueOf(draftId));
                    assertThat(body.findValuesAsText("status")).doesNotContain("DRAFT");
                });

        mockMvc.perform(get("/api/circuits/{id}", draftId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/circuits/{id}", draftId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        mockMvc.perform(patch("/api/circuits/{id}/publish", draftId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/circuits/{id}", draftId)
                        .header("Authorization", bearer(login("demo", "demo123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void adminCanPublishAndCompleteActivity() throws Exception {
        String token = loginAsAdmin();
        long id = createActivity(token, "Tanda publicable", LocalDate.now().plusDays(6));

        mockMvc.perform(patch("/api/circuits/{id}/publish", id)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(patch("/api/circuits/{id}/complete", id)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(patch("/api/circuits/{id}/cancel", id)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidCircuitFieldsAreRejected() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(post("/api/circuits")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activityJson("http://registration.example.test", "09:00", "08:00", 10)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/circuits")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activityJson("https://registration.example.test", "09:00", "08:00", -1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void circuitMutationsRequireAdmin() throws Exception {
        mockMvc.perform(post("/api/circuits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activityJson("https://registration.example.test", "08:00", "09:00", 0)))
                .andExpect(status().isUnauthorized());

        String body = loginAsAdmin();
        mockMvc.perform(post("/api/circuits")
                        .header("Authorization", bearer(login("demo", "demo123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activityJson("https://registration.example.test", "08:00", "09:00", 0)))
                .andExpect(status().isForbidden());
        assertThat(body).isNotBlank();
    }

    @Test
    void schedulerServiceCompletesOnlyPastPublishedActivities() {
        CircuitActivity pastPublished = saveDirect("Pasada publicada", CircuitActivityStatus.PUBLISHED,
                LocalDate.now().minusDays(1));
        CircuitActivity pastDraft = saveDirect("Pasada en borrador", CircuitActivityStatus.DRAFT,
                LocalDate.now().minusDays(1));
        CircuitActivity pastCancelled = saveDirect("Pasada cancelada", CircuitActivityStatus.CANCELLED,
                LocalDate.now().minusDays(1));

        int updated = circuitActivitiesService.completePastCircuitActivities();

        assertThat(updated).isGreaterThanOrEqualTo(1);
        assertThat(circuitActivityRepository.findById(pastPublished.getId()).orElseThrow().getStatus())
                .isEqualTo(CircuitActivityStatus.COMPLETED);
        assertThat(circuitActivityRepository.findById(pastDraft.getId()).orElseThrow().getStatus())
                .isEqualTo(CircuitActivityStatus.DRAFT);
        assertThat(circuitActivityRepository.findById(pastCancelled.getId()).orElseThrow().getStatus())
                .isEqualTo(CircuitActivityStatus.CANCELLED);
    }

    private long createActivity(String token, String title, LocalDate date) throws Exception {
        String response = mockMvc.perform(post("/api/circuits")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activityJson("https://registration.example.test", "08:00", "09:00", 20, title, date)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("id").asLong();
    }

    private CircuitActivity saveDirect(String title, CircuitActivityStatus status, LocalDate date) {
        CircuitActivity activity = new CircuitActivity();
        activity.setTitle(title);
        activity.setCircuitName("Circuito de prueba");
        activity.setDescription("Actividad de prueba");
        activity.setLocation("Madrid");
        activity.setDate(date);
        activity.setStartTime(LocalTime.of(8, 0));
        activity.setEndTime(LocalTime.of(9, 0));
        activity.setPrice(BigDecimal.ZERO);
        activity.setRegistrationUrl("https://registration.example.test");
        activity.setStatus(status);
        activity.setCreatedAt(LocalDateTime.now());
        return circuitActivityRepository.save(activity);
    }

    private String activityJson(String registrationUrl, String startTime, String endTime, double price)
            throws Exception {
        return activityJson(registrationUrl, startTime, endTime, price,
                "Actividad de prueba", LocalDate.now().plusDays(5));
    }

    private String activityJson(String registrationUrl, String startTime, String endTime, double price,
                                String title, LocalDate date) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", title);
        body.put("circuitName", "Circuito de prueba");
        body.put("description", "Actividad de prueba");
        body.put("location", "Madrid");
        body.put("date", date.toString());
        body.put("startTime", startTime);
        body.put("endTime", endTime);
        body.put("price", price);
        body.put("registrationUrl", registrationUrl);
        body.put("informationUrl", "https://info.example.test/activity");
        return objectMapper.writeValueAsString(body);
    }

    private String loginAsAdmin() throws Exception {
        return login("admin", "admin123");
    }

    private String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", username, "password", password));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
