package com.guatequeriders.api;

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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:membershipadmin;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class MembershipAdministrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void administratorHasAnActiveMembershipAndCanSeeOwnCard() throws Exception {
        User admin = userRepository.findByUsername("admin").orElseThrow();
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.isMembershipActive()).isTrue();
        assertThat(admin.getMemberNumber()).isNotBlank();

        mockMvc.perform(get("/api/membership/me/card")
                        .header("Authorization", bearer(login("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberNumber").value(admin.getMemberNumber()))
                .andExpect(jsonPath("$.verificationCode").isNotEmpty());
    }

    @Test
    void temporaryMemberCodeCanBeVerifiedWithoutQr() throws Exception {
        String token = login("admin", "admin123");
        String cardBody = mockMvc.perform(get("/api/membership/me/card")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var card = objectMapper.readTree(cardBody);
        String number = card.get("memberNumber").asText();
        String code = card.get("verificationCode").asText();

        mockMvc.perform(post("/api/membership/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "memberNumber", number,
                                "code", code
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.memberNumber").value(number));

        String wrongCode = code.equals("000000") ? "000001" : "000000";
        mockMvc.perform(post("/api/membership/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "memberNumber", number,
                                "code", wrongCode
                        ))))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanChooseANumberAndDuplicateNumbersAreRejected() throws Exception {
        User first = createUser("number");
        User second = createUser("other");
        String token = login("admin", "admin123");

        markFounder(token, first.getId(), true);
        markFounder(token, second.getId(), true);

        mockMvc.perform(patch("/api/admin/users/{id}/member-number", first.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memberNumber", "000123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberNumber").value("GR-000123"));

        mockMvc.perform(patch("/api/admin/users/{id}/member-number", second.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memberNumber", "GR-000123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Member number already in use"));

        mockMvc.perform(patch("/api/admin/users/{id}/member-number", second.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memberNumber", "invalid number!"))))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findById(second.getId()).orElseThrow().getMemberNumber()).isNull();
    }

    @Test
    void ordinaryMembersKeepAutomaticNumbersAndCannotHaveManualNumbers() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String username = "regular-" + unique;
        String phone = String.format("6%08d", Math.floorMod(UUID.randomUUID().hashCode(), 100_000_000));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "email", username + "@example.com",
                                "phone", phone,
                                "password", "testpassword123",
                                "privacyAccepted", true
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.memberActive").value(false))
                .andExpect(jsonPath("$.user.founder").value(false))
                .andExpect(jsonPath("$.user.memberNumber").isEmpty());

        User person = userRepository.findByUsername(username).orElseThrow();
        assertThat(person.getMemberNumber()).isNull();

        mockMvc.perform(get("/api/membership/me/card")
                        .header("Authorization", bearer(login(username, "testpassword123"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/admin/users/{id}/membership", person.getId())
                        .header("Authorization", bearer(login("admin", "admin123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberActive").value(true))
                .andExpect(jsonPath("$.memberNumber").isNotEmpty());

        String autoNumber = userRepository.findById(person.getId()).orElseThrow().getMemberNumber();

        mockMvc.perform(patch("/api/admin/users/{id}/member-number", person.getId())
                        .header("Authorization", bearer(login("admin", "admin123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memberNumber", "GR-0001"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Manual member numbers are reserved for founders"));

        assertThat(userRepository.findById(person.getId()).orElseThrow().getMemberNumber()).isEqualTo(autoNumber);
    }

    @Test
    void removingFounderStatusPreventsFutureManualNumberChanges() throws Exception {
        User person = createUser("founder-remove");
        String token = login("admin", "admin123");
        markFounder(token, person.getId(), true);

        mockMvc.perform(patch("/api/admin/users/{id}/member-number", person.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memberNumber", "GR-0099"))))
                .andExpect(status().isOk());

        markFounder(token, person.getId(), false);

        mockMvc.perform(patch("/api/admin/users/{id}/member-number", person.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memberNumber", "GR-0100"))))
                .andExpect(status().isConflict());

        assertThat(userRepository.findById(person.getId()).orElseThrow().getMemberNumber()).isEqualTo("GR-0099");
    }

    @Test
    void nonAdminCannotChangeFounderStatus() throws Exception {
        User person = createUser("not-founder");
        mockMvc.perform(patch("/api/admin/users/{id}/founder", person.getId())
                        .header("Authorization", bearer(login(person.getUsername(), "test12345")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("founder", true))))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotChangeMembershipNumber() throws Exception {
        User person = createUser("restricted");
        String token = login(person.getUsername(), "test12345");

        mockMvc.perform(patch("/api/admin/users/{id}/member-number", person.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memberNumber", "GR-0011"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void promotedAdminReceivesMembershipAndKeepsAdminPrivileges() throws Exception {
        User person = createUser("promoted");
        String adminToken = login("admin", "admin123");

        mockMvc.perform(patch("/api/admin/users/{id}/role", person.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberActive").value(true))
                .andExpect(jsonPath("$.memberNumber").isNotEmpty());

        String promotedToken = login(person.getUsername(), "test12345");
        mockMvc.perform(get("/api/membership/me/card")
                        .header("Authorization", bearer(promotedToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", bearer(promotedToken)))
                .andExpect(status().isOk());
    }

    @Test
    void adminMembershipCannotBeDisabled() throws Exception {
        User admin = userRepository.findByUsername("admin").orElseThrow();

        mockMvc.perform(patch("/api/admin/users/{id}/membership", admin.getId())
                        .header("Authorization", bearer(login("admin", "admin123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Administrators must remain active members"));
    }

    private void markFounder(String token, Long userId, boolean founder) throws Exception {
        mockMvc.perform(patch("/api/admin/users/{id}/founder", userId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("founder", founder))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.founder").value(founder));
    }

    private User createUser(String prefix) {
        String name = prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername(name);
        user.setEmail(name + "@example.com");
        user.setPassword(passwordEncoder.encode("test12345"));
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
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
