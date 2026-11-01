package com.guatequeriders.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:securityhardening;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "app.rate-limit.enabled=false"
})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class SecurityHardeningIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void anonymousAndNormalUsersCannotUseAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());

        String demoToken = login("demo", "demo123");
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", bearer(demoToken)))
                .andExpect(status().isForbidden());

        String adminToken = login("admin", "admin123");
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotReadOrCancelAnotherUsersOrder() throws Exception {
        String adminToken = login("admin", "admin123");
        String demoToken = login("demo", "demo123");

        mockMvc.perform(post("/api/admin/merch/campaigns")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isCreated());

        String orderResponse = mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(demoToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "customerName", "Demo",
                                "customerEmail", "demo-order@example.com",
                                "customerPhone", "+34123456789",
                                "merchTermsAccepted", true,
                                "items", List.of(Map.of(
                                        "productCode", "TSHIRT",
                                        "size", "M",
                                        "quantity", 1
                                ))
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

        String otherUsername = "other-" + UUID.randomUUID().toString().substring(0, 8);
        createUser(otherUsername, "other-password-123");
        String otherToken = login(otherUsername, "other-password-123");

        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrationRejectsWeakPasswordsAndAcceptsLongPassphrases() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "weak-" + suffix,
                                "email", "weak-" + suffix + "@example.com",
                                "password", "short123",
                                "privacyAccepted", true
                        ))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "strong-" + suffix,
                                "email", "strong-" + suffix + "@example.com",
                                "password", "una frase larga segura 2026",
                                "privacyAccepted", true
                        ))))
                .andExpect(status().isCreated());
    }

    @Test
    void backendSendsSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/health").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")))
                .andExpect(header().string("Strict-Transport-Security", containsString("max-age=31536000")));
    }

    private User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
