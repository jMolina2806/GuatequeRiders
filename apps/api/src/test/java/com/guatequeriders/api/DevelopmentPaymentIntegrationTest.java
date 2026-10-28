package com.guatequeriders.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guatequeriders.api.merch.model.CampaignStatus;
import com.guatequeriders.api.merch.model.MerchCampaign;
import com.guatequeriders.api.merch.repository.MerchCampaignRepository;
import com.guatequeriders.api.orders.repository.OrderRepository;
import com.guatequeriders.api.orders.repository.SupplierOrderBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:developmentpayments;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "app.environment=development",
        "app.merch.test-payments-enabled=true"
})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class DevelopmentPaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SupplierOrderBatchRepository batchRepository;

    @Autowired
    private MerchCampaignRepository campaignRepository;

    @BeforeEach
    void resetMerch() {
        batchRepository.deleteAll();
        orderRepository.deleteAll();
        campaignRepository.deleteAll();

        MerchCampaign campaign = new MerchCampaign();
        campaign.setStatus(CampaignStatus.CLOSED);
        campaign.setOpenedAt(LocalDateTime.now());
        campaignRepository.save(campaign);
    }

    @Test
    void userCanSimulateOnlyTheirOwnPendingOrderInDevelopment() throws Exception {
        long campaignId = openCampaign();
        String demoToken = login("demo", "demo123");
        long orderId = createOrder(demoToken);

        mockMvc.perform(get("/api/dev/payments/status")
                        .header("Authorization", bearer(demoToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.environment").value("development"));

        mockMvc.perform(post("/api/dev/orders/{id}/simulate-payment", orderId)
                        .header("Authorization", bearer(demoToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.campaignId").value(campaignId));

        mockMvc.perform(post("/api/dev/orders/{id}/simulate-payment", orderId)
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isForbidden());

        long pendingAfterCloseId = createOrder(demoToken);
        mockMvc.perform(post("/api/admin/merch/campaigns/{id}/close", campaignId)
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/dev/orders/{id}/simulate-payment", pendingAfterCloseId)
                        .header("Authorization", bearer(demoToken)))
                .andExpect(status().isConflict());
    }

    private long openCampaign() throws Exception {
        String response = mockMvc.perform(post("/api/admin/merch/campaigns")
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createOrder(String token) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customerName", "Cliente de desarrollo");
        body.put("customerEmail", "dev@example.com");
        body.put("customerPhone", "+34123456789");
        body.put("items", java.util.List.of(Map.of(
                "productCode", "TSHIRT",
                "size", "M",
                "quantity", 1
        )));

        String response = mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }

    private String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
