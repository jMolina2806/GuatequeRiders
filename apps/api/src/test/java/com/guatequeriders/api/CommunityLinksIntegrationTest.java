package com.guatequeriders.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:communitylinks;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "app.community.whatsapp-group-url=https://chat.whatsapp.com/test-group"
})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class CommunityLinksIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void anonymousUsersCannotReadPrivateCommunityLinks() throws Exception {
        mockMvc.perform(get("/api/community/links"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUsersCanReadPrivateCommunityLinks() throws Exception {
        String token = login("demo", "demo123");

        mockMvc.perform(get("/api/community/links")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whatsappGroupUrl")
                        .value("https://chat.whatsapp.com/test-group"));
    }

    private String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password));

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
