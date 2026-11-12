package com.guatequeriders.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:merchprotection;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "app.environment=production",
        "app.merch.new-campaigns-enabled=true",
        "stripe.enabled=false"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MerchProductionReadinessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void productionStartsWithoutSalesAndBlocksOpeningWithoutLiveStripe() throws Exception {
        String username = "admin-" + UUID.randomUUID().toString().substring(0, 8);
        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(username + "@example.com");
        admin.setPassword(passwordEncoder.encode("strong-test-password"));
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.saveAndFlush(admin);

        String tokenResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", "strong-test-password"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(tokenResponse).get("token").asText();

        mockMvc.perform(get("/api/admin/merch/campaigns/readiness"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/merch/campaigns/readiness")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canOpen").value(false))
                .andExpect(jsonPath("$.blockers[0]").value(
                        "Los cobros de Stripe en modo real deben estar configurados antes de abrir ventas."
                ));

        mockMvc.perform(post("/api/admin/merch/campaigns")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Real Stripe payments must be configured before opening a Merch campaign"
                ));

        mockMvc.perform(get("/api/merch/campaign/current"))
                .andExpect(status().isNotFound());
    }
}
