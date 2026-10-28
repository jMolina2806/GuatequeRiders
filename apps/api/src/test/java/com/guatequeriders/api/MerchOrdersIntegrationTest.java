package com.guatequeriders.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guatequeriders.api.merch.model.CampaignStatus;
import com.guatequeriders.api.merch.model.MerchCampaign;
import com.guatequeriders.api.merch.repository.MerchCampaignRepository;
import com.guatequeriders.api.orders.model.Order;
import com.guatequeriders.api.orders.model.OrderItem;
import com.guatequeriders.api.orders.model.OrderStatus;
import com.guatequeriders.api.orders.model.PaymentStatus;
import com.guatequeriders.api.orders.repository.OrderRepository;
import com.guatequeriders.api.orders.repository.SupplierOrderBatchRepository;
import com.guatequeriders.api.orders.service.OrdersService;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityManager;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:merchorders;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class MerchOrdersIntegrationTest {

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private EntityManager entityManager;

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
    void currentCampaignIsPublicAndOnlyAdminCanOpenCampaign() throws Exception {
        mockMvc.perform(get("/api/merch/campaign/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.acceptingOrders").value(false))
                .andExpect(jsonPath("$.acceptingPayments").value(false));

        mockMvc.perform(post("/api/admin/merch/campaigns")
                        .header("Authorization", bearer(login("demo", "demo123"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/merch/campaigns")
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.acceptingOrders").value(true))
                .andExpect(jsonPath("$.acceptingPayments").value(true));
    }

    @Test
    void developmentPaymentEndpointIsAbsentWithoutDevelopmentConfiguration() throws Exception {
        mockMvc.perform(get("/api/dev/payments/status")
                        .header("Authorization", bearer(login("demo", "demo123"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void onlyOneCampaignCanBeOpenAndUserCannotAccessHistory() throws Exception {
        openCampaign();

        mockMvc.perform(post("/api/admin/merch/campaigns")
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/admin/merch/campaigns")
                        .header("Authorization", bearer(login("demo", "demo123"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void checkoutRequiresAuthenticationAndCampaignOpen() throws Exception {
        String body = orderJson("TSHIRT", "M", 1);

        mockMvc.perform(post("/api/orders")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(login("demo", "demo123")))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());

        long campaignId = openCampaign();
        String demoToken = login("demo", "demo123");
        long orderId = createOrder(demoToken, body);
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getCampaign().getId()).isEqualTo(campaignId);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void backendCalculatesCatalogPricesAndIgnoresClientAmounts() throws Exception {
        openCampaign();
        Map<String, Object> firstItem = new LinkedHashMap<>();
        firstItem.put("productCode", "TSHIRT");
        firstItem.put("size", "M");
        firstItem.put("quantity", 2);
        firstItem.put("unitPrice", 0);
        firstItem.put("subtotal", 0);
        Map<String, Object> secondItem = new LinkedHashMap<>();
        secondItem.put("productCode", "CROP_TOP");
        secondItem.put("size", "L");
        secondItem.put("quantity", 1);

        Map<String, Object> body = customerFields();
        body.put("items", List.of(firstItem, secondItem));
        body.put("total", 0);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(login("demo", "demo123")))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(52.0))
                .andExpect(jsonPath("$.campaignId").isNumber())
                .andExpect(jsonPath("$.latePayment").value(false))
                .andExpect(jsonPath("$.items[0].unitPrice").value(20.0))
                .andExpect(jsonPath("$.items[1].unitPrice").value(12.0));
    }

    @Test
    void customNameIsTrimmedAndInvalidValuesAreRejected() throws Exception {
        openCampaign();
        String token = login("demo", "demo123");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(List.of(item("TSHIRT", "M", 1, "  JAVIER  ")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].customName").value("JAVIER"));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(List.of(item("TSHIRT", "M", 1, "javi_molina16")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].customName").value("javi_molina16"));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(List.of(item("CROP_TOP", "S", 1, "123456789012345678901")))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(List.of(item("CROP_TOP", "S", 1, "<b>JAVIER</b>")))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeSupplierOrderPreservesPersonalizationsAndExcludesPendingOrders() throws Exception {
        long campaignId = openCampaign();
        String token = login("demo", "demo123");

        long shirtOrderId = createOrder(token, orderJson(List.of(item("TSHIRT", "M", 1, "JAVIER"))));
        markPaid(shirtOrderId);

        long hoodieOrderId = createOrder(token, orderJson(List.of(
                item("HOODIE", "L", 1, "MARCOS"),
                item("HOODIE", "L", 1, null)
        )));
        markPaid(hoodieOrderId);

        long cropTopOrderId = createOrder(token, orderJson(List.of(item("CROP_TOP", "S", 1, "LAURA"))));
        markPaid(cropTopOrderId);
        long pendingOrderId = createOrder(token, orderJson(List.of(item("CROP_TOP", "S", 1, null))));

        Order hoodieOrder = orderRepository.findWithDetailsById(hoodieOrderId).orElseThrow();
        assertThat(hoodieOrder.getItems()).hasSize(2);
        assertThat(hoodieOrder.getItems()).extracting(OrderItem::getCustomName)
                .containsExactlyInAnyOrder("MARCOS", null);

        String closeResponse = mockMvc.perform(post("/api/admin/merch/campaigns/{id}/close", campaignId)
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supplierOrder.orders.length()").value(3))
                .andExpect(jsonPath("$.supplierOrder.totalUnits").value(4))
                .andExpect(jsonPath("$.supplierOrder.personalizations.length()").value(3))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode supplierOrder = objectMapper.readTree(closeResponse).get("supplierOrder");
        assertThat(summaryQuantity(supplierOrder, "TSHIRT", "M")).isEqualTo(1);
        assertThat(summaryQuantity(supplierOrder, "HOODIE", "L")).isEqualTo(2);
        assertThat(summaryQuantity(supplierOrder, "CROP_TOP", "S")).isEqualTo(1);
        List<Long> supplierOrderIds = new java.util.ArrayList<>();
        supplierOrder.get("orders").forEach(order -> supplierOrderIds.add(order.get("orderId").asLong()));
        assertThat(supplierOrderIds).doesNotContain(pendingOrderId);

        List<String> personalizationNames = new java.util.ArrayList<>();
        supplierOrder.get("personalizations").forEach(personalization ->
                personalizationNames.add(personalization.get("customName").asText()));
        assertThat(personalizationNames).containsExactlyInAnyOrder("JAVIER", "MARCOS", "LAURA");

        JsonNode hoodieSupplierOrder = findSupplierOrder(supplierOrder, hoodieOrderId);
        assertThat(hoodieSupplierOrder.get("items")).hasSize(2);
        assertThat(hoodieSupplierOrder.get("items").toString()).contains("MARCOS");
        assertThat(hoodieSupplierOrder.get("items").toString()).contains("customName");

        Order shirtOrder = orderRepository.findWithDetailsById(shirtOrderId).orElseThrow();
        shirtOrder.getItems().get(0).setCustomName("MUTATED");
        orderRepository.saveAndFlush(shirtOrder);
        entityManager.clear();

        assertThat(orderRepository.findWithDetailsById(shirtOrderId).orElseThrow().getItems().get(0).getCustomName())
                .isEqualTo("JAVIER");
        mockMvc.perform(get("/api/admin/merch/campaigns/{id}/supplier-order", campaignId)
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalizations[?(@.customName == 'JAVIER')].quantity").value(1));
    }

    @Test
    void openCampaignAllowsPaymentStartAndClosedCampaignRejectsIt() throws Exception {
        openCampaign();
        long orderId = createOrder(login("demo", "demo123"), orderJson("HOODIE", "XL", 1));
        ordersService.assertPaymentCanStart(orderId);

        closeCurrentCampaign();

        assertThatThrownBy(() -> ordersService.assertPaymentCanStart(orderId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("New payments are closed for this Merch campaign");
    }

    @Test
    void closingCreatesSnapshotWithOnlyPaidOrdersAndLeavesPendingHistory() throws Exception {
        long campaignId = openCampaign();
        String demoToken = login("demo", "demo123");
        long paidOrderId = createOrder(demoToken, orderJson(List.of(
                Map.of("productCode", "TSHIRT", "size", "M", "quantity", 2),
                Map.of("productCode", "HOODIE", "size", "L", "quantity", 1)
        )));
        markPaid(paidOrderId);
        long secondPaidOrderId = createOrder(demoToken, orderJson("TSHIRT", "M", 4));
        markPaid(secondPaidOrderId);
        long pendingOrderId = createOrder(demoToken, orderJson("CROP_TOP", "S", 4));

        String closeResponse = mockMvc.perform(post("/api/admin/merch/campaigns/{id}/close", campaignId)
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaign.status").value("CLOSED"))
                .andExpect(jsonPath("$.supplierOrder.campaignId").value(campaignId))
                .andExpect(jsonPath("$.supplierOrder.orders.length()").value(2))
                .andExpect(jsonPath("$.supplierOrder.totalUnits").value(7))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode closeBody = objectMapper.readTree(closeResponse);
        assertThat(closeBody.at("/supplierOrder/orders/0/orderId").asLong())
                .isIn(paidOrderId, secondPaidOrderId);

        Order pendingOrder = orderRepository.findById(pendingOrderId).orElseThrow();
        assertThat(pendingOrder.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(batchRepository.findByCampaignId(campaignId)).isPresent();

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(demoToken))
                        .contentType(APPLICATION_JSON)
                        .content(orderJson("HOODIE", "S", 1)))
                .andExpect(status().isConflict());
    }

    @Test
    void provisionalDashboardUsesOnlyPaidAndAggregatesAcrossOrders() throws Exception {
        long campaignId = openCampaign();
        String demoToken = login("demo", "demo123");
        long firstPaid = createOrder(demoToken, orderJson("TSHIRT", "M", 2));
        markPaid(firstPaid);
        long secondPaid = createOrder(demoToken, orderJson("TSHIRT", "M", 4));
        markPaid(secondPaid);
        createOrder(demoToken, orderJson("CROP_TOP", "S", 5));

        String response = mockMvc.perform(get("/api/admin/merch/campaigns/{id}/dashboard", campaignId)
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaign.status").value("OPEN"))
                .andExpect(jsonPath("$.stats.totalOrders").value(3))
                .andExpect(jsonPath("$.stats.paidOrders").value(2))
                .andExpect(jsonPath("$.stats.pendingOrders").value(1))
                .andExpect(jsonPath("$.stats.paidUnits").value(6))
                .andExpect(jsonPath("$.provisionalPreparation.totalUnits").value(6))
                .andExpect(jsonPath("$.provisionalPreparation.orders.length()").value(2))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode preparation = objectMapper.readTree(response).get("provisionalPreparation");
        assertThat(summaryQuantity(preparation, "TSHIRT", "M")).isEqualTo(6);
        assertThat(summaryQuantity(preparation, "CROP_TOP", "S")).isZero();
    }

    @Test
    void lateWebhookUpdatesPaymentButNeverChangesFrozenSupplierOrder() throws Exception {
        long campaignId = openCampaign();
        String demoToken = login("demo", "demo123");
        long pendingOrderId = createOrder(demoToken, orderJson("HOODIE", "L", 1));
        closeCurrentCampaign();

        Order lateOrder = ordersService.applyPaymentWebhook(pendingOrderId);
        assertThat(lateOrder.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(lateOrder.isLatePayment()).isTrue();

        String dashboard = mockMvc.perform(get("/api/admin/merch/campaigns/{id}/dashboard", campaignId)
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supplierOrder.orders.length()").value(0))
                .andExpect(jsonPath("$.latePaymentOrders.length()").value(1))
                .andExpect(jsonPath("$.latePaymentOrders[0].latePayment").value(true))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(objectMapper.readTree(dashboard).at("/supplierOrder/totalUnits").asInt()).isZero();
    }

    @Test
    void snapshotIsImmutableAndCampaignCanBeMarkedOrdered() throws Exception {
        long campaignId = openCampaign();
        long orderId = createOrder(login("demo", "demo123"), orderJson("TSHIRT", "S", 2));
        markPaid(orderId);
        closeCurrentCampaign();

        Order liveOrder = orderRepository.findById(orderId).orElseThrow();
        liveOrder.setCustomerName("Nombre modificado");
        liveOrder.setPaymentStatus(PaymentStatus.CANCELLED);
        orderRepository.save(liveOrder);

        mockMvc.perform(get("/api/admin/merch/campaigns/{id}/supplier-order", campaignId)
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].customerName").value("Cliente de prueba"))
                .andExpect(jsonPath("$.orders[0].items[0].quantity").value(2));

        mockMvc.perform(post("/api/admin/merch/campaigns/{id}/ordered", campaignId)
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ORDERED"));
    }

    @Test
    void newCampaignDoesNotMixOrdersFromPreviousCampaign() throws Exception {
        long firstCampaign = openCampaign();
        long firstOrder = createOrder(login("demo", "demo123"), orderJson("TSHIRT", "S", 1));
        markPaid(firstOrder);
        closeCurrentCampaign();

        long secondCampaign = openCampaign();
        createOrder(login("demo", "demo123"), orderJson("HOODIE", "XL", 1));

        mockMvc.perform(get("/api/admin/merch/campaigns/{id}/dashboard", secondCampaign)
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.totalOrders").value(1))
                .andExpect(jsonPath("$.stats.paidOrders").value(0));

        mockMvc.perform(get("/api/admin/merch/campaigns/{id}/supplier-order", firstCampaign)
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(1))
                .andExpect(jsonPath("$.orders[0].orderId").value(firstOrder));
    }

    @Test
    void failedCloseRollsBackCampaignAndSupplierBatch() throws Exception {
        long campaignId = openCampaign();
        String token = login("demo", "demo123");
        long orderId = createOrder(token, orderJson("TSHIRT", "M", 1));
        markPaid(orderId);
        Order order = orderRepository.findWithDetailsById(orderId).orElseThrow();
        OrderItem item = order.getItems().get(0);
        item.setProductCode("INVALID");
        orderRepository.save(order);

        mockMvc.perform(post("/api/admin/merch/campaigns/{id}/close", campaignId)
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isBadRequest());

        assertThat(campaignRepository.findById(campaignId).orElseThrow().getStatus())
                .isEqualTo(CampaignStatus.OPEN);
        assertThat(batchRepository.findByCampaignId(campaignId)).isEmpty();
    }

    private long openCampaign() throws Exception {
        String response = mockMvc.perform(post("/api/admin/merch/campaigns")
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void closeCurrentCampaign() throws Exception {
        long campaignId = campaignRepository.findTopByOrderByIdDesc().orElseThrow().getId();
        mockMvc.perform(post("/api/admin/merch/campaigns/{id}/close", campaignId)
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isOk());
    }

    private void markPaid(long orderId) {
        ordersService.applyPaymentWebhook(orderId);
    }

    private long createOrder(String token, String body) throws Exception {
        String response = mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String orderJson(String productCode, String size, int quantity) throws Exception {
        return orderJson(List.of(Map.of(
                "productCode", productCode,
                "size", size,
                "quantity", quantity)));
    }

    private String orderJson(List<Map<String, Object>> items) throws Exception {
        Map<String, Object> body = customerFields();
        body.put("items", items);
        return objectMapper.writeValueAsString(body);
    }

    private Map<String, Object> item(String productCode, String size, int quantity, String customName) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("productCode", productCode);
        item.put("size", size);
        item.put("quantity", quantity);
        item.put("customName", customName);
        return item;
    }

    private JsonNode findSupplierOrder(JsonNode supplierOrder, long orderId) {
        for (JsonNode order : supplierOrder.get("orders")) {
            if (order.get("orderId").asLong() == orderId) {
                return order;
            }
        }
        throw new AssertionError("Supplier order not found: " + orderId);
    }

    private int summaryQuantity(JsonNode preparation, String productCode, String size) {
        for (JsonNode line : preparation.get("summary")) {
            if (productCode.equals(line.get("productCode").asText())
                    && size.equals(line.get("size").asText())) {
                return line.get("quantity").asInt();
            }
        }
        throw new AssertionError("Summary line not found: " + productCode + " / " + size);
    }

    private Map<String, Object> customerFields() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customerName", "Cliente de prueba");
        body.put("customerEmail", "cliente@example.com");
        body.put("customerPhone", "+34123456789");
        body.put("notes", "Entrega coordinada");
        return body;
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
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
        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
