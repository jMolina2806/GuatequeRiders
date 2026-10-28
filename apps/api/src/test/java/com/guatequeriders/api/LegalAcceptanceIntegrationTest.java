package com.guatequeriders.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guatequeriders.api.legal.LegalVersions;
import com.guatequeriders.api.orders.model.Order;
import com.guatequeriders.api.orders.repository.OrderRepository;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:legalacceptance;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class LegalAcceptanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void registrationRequiresPrivacyAcknowledgementAndStoresVersion() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "legal-" + suffix;
        String email = username + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "email", email,
                                "password", "password123",
                                "privacyAccepted", false
                        ))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "email", email,
                                "password", "password123",
                                "privacyAccepted", true
                        ))))
                .andExpect(status().isCreated());

        User user = userRepository.findByUsername(username).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(user.getPrivacyAcknowledgedAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(user.getPrivacyPolicyVersion())
                .isEqualTo(LegalVersions.PRIVACY_POLICY);
    }

    @Test
    void orderRequiresMerchTermsAcceptanceAndStoresVersion() throws Exception {
        String adminToken = login("admin", "admin123");
        String userToken = login("demo", "demo123");

        mockMvc.perform(post("/api/admin/merch/campaigns")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isCreated());

        Map<String, Object> baseOrder = Map.of(
                "customerName", "Legal Test",
                "customerEmail", "legal-order@example.com",
                "customerPhone", "+34123456789",
                "items", List.of(Map.of(
                        "productCode", "TSHIRT",
                        "size", "M",
                        "quantity", 1
                ))
        );

        Map<String, Object> rejectedOrder = new java.util.LinkedHashMap<>(baseOrder);
        rejectedOrder.put("merchTermsAccepted", false);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectedOrder)))
                .andExpect(status().isBadRequest());

        Map<String, Object> acceptedOrder = new java.util.LinkedHashMap<>(baseOrder);
        acceptedOrder.put("merchTermsAccepted", true);

        String response = mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptedOrder)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(response);
        Order order = orderRepository.findById(json.get("id").asLong()).orElseThrow();

        org.assertj.core.api.Assertions.assertThat(order.getMerchTermsAcceptedAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(order.getMerchTermsVersion())
                .isEqualTo(LegalVersions.MERCH_TERMS);
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
        return objectMapper.readTree(response).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
