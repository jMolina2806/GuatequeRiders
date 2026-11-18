package com.guatequeriders.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guatequeriders.api.notifications.model.NotificationCategory;
import com.guatequeriders.api.notifications.service.UnsubscribeTokenService;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:notificationpreferences;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class NotificationPreferencesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UnsubscribeTokenService unsubscribeTokenService;

    @Test
    void userCanManagePreferencesAndPublicUnsubscribeDisablesOnlyRequestedCategory() throws Exception {
        String authToken = login("demo", "demo123");

        mockMvc.perform(get("/api/notifications/preferences")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchNotifications").value(false))
                .andExpect(jsonPath("$.routeNotifications").value(false))
                .andExpect(jsonPath("$.eventNotifications").value(false))
                .andExpect(jsonPath("$.circuitNotifications").value(false));

        mockMvc.perform(put("/api/notifications/preferences")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchNotifications": true,
                                  "routeNotifications": true,
                                  "eventNotifications": false,
                                  "circuitNotifications": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchNotifications").value(true))
                .andExpect(jsonPath("$.routeNotifications").value(true))
                .andExpect(jsonPath("$.eventNotifications").value(false))
                .andExpect(jsonPath("$.circuitNotifications").value(true));

        User user = userRepository.findByUsername("demo").orElseThrow();
        String unsubscribeToken = unsubscribeTokenService.create(
                user.getId(),
                NotificationCategory.ROUTES
        );

        mockMvc.perform(post("/api/notifications/unsubscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", unsubscribeToken,
                                "category", "ROUTES"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unsubscribed").value(true))
                .andExpect(jsonPath("$.category").value("ROUTES"));

        mockMvc.perform(get("/api/notifications/preferences")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchNotifications").value(true))
                .andExpect(jsonPath("$.routeNotifications").value(false))
                .andExpect(jsonPath("$.eventNotifications").value(false))
                .andExpect(jsonPath("$.circuitNotifications").value(true));
    }

    private String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password
        ));

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response).get("token").asText();
    }
}
