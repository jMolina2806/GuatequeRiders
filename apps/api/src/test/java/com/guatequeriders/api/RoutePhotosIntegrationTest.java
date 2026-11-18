package com.guatequeriders.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guatequeriders.api.config.UploadProperties;
import com.guatequeriders.api.registrations.model.RouteRegistration;
import com.guatequeriders.api.registrations.repository.RouteRegistrationRepository;
import com.guatequeriders.api.routes.model.Difficulty;
import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.model.RouteStatus;
import com.guatequeriders.api.routes.photos.model.RoutePhoto;
import com.guatequeriders.api.routes.photos.repository.RoutePhotoRepository;
import com.guatequeriders.api.routes.repository.RouteRepository;
import com.guatequeriders.api.routes.photos.service.RoutePhotoStorageService;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import com.guatequeriders.api.users.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:routephotos;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "app.upload-dir=target/test-uploads/route-photos"
})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class RoutePhotosIntegrationTest {

    private static final byte[] PNG = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RouteRegistrationRepository registrationRepository;

    @Autowired
    private RoutePhotoRepository photoRepository;

    @Autowired
    private UploadProperties uploadProperties;

    @Autowired
    private RoutePhotoStorageService storageService;

    private final List<Long> createdRouteIds = new ArrayList<>();

    @AfterEach
    void cleanTestData() throws IOException {
        for (Long routeId : createdRouteIds) {
            photoRepository.deleteAll(photoRepository.findAllByRouteIdOrderByCreatedAtAscIdAsc(routeId));
            registrationRepository.findAllByRouteId(routeId).forEach(registrationRepository::delete);
            if (routeRepository.existsById(routeId)) {
                routeRepository.deleteById(routeId);
            }
            storageService.deleteRouteDirectory(routeId);
        }
        createdRouteIds.clear();
        deleteDirectoryIfEmpty(uploadProperties.getRoutesRoot());
    }

    @Test
    void publicGalleryUsesInlineForFilesAndAttachmentForDownloads() throws Exception {
        long routeId = createRoute(RouteStatus.PUBLISHED);
        String adminToken = login("admin", "admin123");

        String uploadResponse = upload(adminToken, routeId, "ruta final.png", "image/png", PNG)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageUrl").value(containsString(
                        "/api/routes/" + routeId + "/photos/")))
                .andExpect(jsonPath("$.downloadUrl").value(containsString(
                        "/api/routes/" + routeId + "/photos/")))
                .andExpect(jsonPath("$.uploadedBy.username").value("admin"))
                .andExpect(jsonPath("$.uploadedBy.email").doesNotExist())
                .andExpect(jsonPath("$.storedFilename").doesNotExist())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        long photoId = objectMapper.readTree(uploadResponse).get("id").asLong();

        mockMvc.perform(get("/api/routes/{routeId}/photos", routeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(photoId))
                .andExpect(jsonPath("$[0].uploadedBy.username").value("admin"));

        mockMvc.perform(get("/api/routes/{routeId}/photos/{photoId}/file", routeId, photoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(header().string("Content-Disposition", containsString("inline")))
                .andExpect(content().bytes(PNG));

        mockMvc.perform(get("/api/routes/{routeId}/photos/{photoId}/download", routeId, photoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition", containsString(
                        "guatequeriders-route-" + routeId + "-photo-" + photoId + ".png")))
                .andExpect(content().bytes(PNG));
    }

    @Test
    void downloadContentDispositionDoesNotUseUntrustedOriginalNames() throws Exception {
        long routeId = createRoute(RouteStatus.PUBLISHED);
        String adminToken = login("admin", "admin123");
        String[] originalNames = {
                "foto.jpg\r\nX-Test: injected",
                "../../foto.jpg",
                "\"foto.jpg",
                "nombre-unicode-árbol.png"
        };

        for (String originalName : originalNames) {
            String response = upload(adminToken, routeId, originalName, "image/png", PNG)
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
            long photoId = objectMapper.readTree(response).get("id").asLong();

            String contentDisposition = mockMvc.perform(get(
                            "/api/routes/{routeId}/photos/{photoId}/download", routeId, photoId))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", containsString("attachment")))
                    .andReturn().getResponse().getHeader("Content-Disposition");

            assertThat(contentDisposition)
                    .isNotNull()
                    .doesNotContain("\r")
                    .doesNotContain("\n")
                    .doesNotContain("X-Test")
                    .doesNotContain("foto")
                    .contains("guatequeriders-route-" + routeId + "-photo-" + photoId + ".png");
        }
    }

    @Test
    void draftGalleryIsHiddenFromPublicButAvailableToAdmin() throws Exception {
        long routeId = createRoute(RouteStatus.DRAFT);
        String adminToken = login("admin", "admin123");
        String uploadResponse = upload(adminToken, routeId, "draft.png", "image/png", PNG)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        long photoId = objectMapper.readTree(uploadResponse).get("id").asLong();

        mockMvc.perform(get("/api/routes/{routeId}/photos", routeId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/routes/{routeId}/photos/{photoId}/file", routeId, photoId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/routes/{routeId}/photos", routeId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(photoId));
    }

    @Test
    void uploadAndDeletePermissionsFollowRouteRules() throws Exception {
        long routeId = createRoute(RouteStatus.IN_PROGRESS);
        User demo = userRepository.findByUsername("demo").orElseThrow();
        register(routeId, demo);
        User unregistered = createUser("unregistered-" + UUID.randomUUID());
        String demoToken = login("demo", "demo123");
        String unregisteredToken = login(unregistered.getUsername(), "password123");
        String adminToken = login("admin", "admin123");

        String response = upload(demoToken, routeId, "demo.png", "image/png", PNG)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        long photoId = objectMapper.readTree(response).get("id").asLong();

        upload(null, routeId, "guest.png", "image/png", PNG)
                .andExpect(status().isUnauthorized());
        upload(unregisteredToken, routeId, "unregistered.png", "image/png", PNG)
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/routes/{routeId}/photos/{photoId}", routeId, photoId)
                        .header("Authorization", bearer(unregisteredToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/routes/{routeId}/photos/{photoId}", routeId, photoId)
                        .header("Authorization", bearer(demoToken)))
                .andExpect(status().isNoContent());

        long draftRouteId = createRoute(RouteStatus.DRAFT);
        upload(adminToken, draftRouteId, "admin.png", "image/png", PNG)
                .andExpect(status().isCreated());
    }

    @Test
    void userCannotUploadMoreThanTwentyPhotosPerRoute() throws Exception {
        long routeId = createRoute(RouteStatus.COMPLETED);
        User demo = userRepository.findByUsername("demo").orElseThrow();
        register(routeId, demo);
        String demoToken = login("demo", "demo123");

        for (int index = 0; index < 20; index++) {
            upload(demoToken, routeId, "photo-" + index + ".png", "image/png", PNG)
                    .andExpect(status().isCreated());
        }

        upload(demoToken, routeId, "photo-21.png", "image/png", PNG)
                .andExpect(status().isConflict());
    }

    @Test
    void invalidContentAndRouteDeletionCleanPhysicalFilesSafely() throws Exception {
        long routeId = createRoute(RouteStatus.PUBLISHED);
        String adminToken = login("admin", "admin123");

        upload(adminToken, routeId, "wrong.png", "image/png", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})
                .andExpect(status().isBadRequest());

        String firstResponse = upload(adminToken, routeId, "first.png", "image/png", PNG)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        long firstPhotoId = objectMapper.readTree(firstResponse).get("id").asLong();
        RoutePhoto firstPhoto = photoRepository.findById(firstPhotoId).orElseThrow();
        Path firstPath = uploadProperties.getRoutesRoot()
                .resolve(String.valueOf(routeId)).resolve(firstPhoto.getStoredFilename());

        mockMvc.perform(delete("/api/routes/{routeId}/photos/{photoId}", routeId, firstPhotoId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());
        assertThat(Files.exists(firstPath)).isFalse();

        String secondResponse = upload(adminToken, routeId, "second.png", "image/png", PNG)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        long secondPhotoId = objectMapper.readTree(secondResponse).get("id").asLong();
        RoutePhoto secondPhoto = photoRepository.findById(secondPhotoId).orElseThrow();
        Path secondPath = uploadProperties.getRoutesRoot()
                .resolve(String.valueOf(routeId)).resolve(secondPhoto.getStoredFilename());
        assertThat(Files.exists(secondPath)).isTrue();

        mockMvc.perform(delete("/api/routes/{id}", routeId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());
        assertThat(routeRepository.existsById(routeId)).isFalse();
        assertThat(Files.exists(secondPath)).isFalse();
        assertThat(Files.exists(uploadProperties.getRoutesRoot().resolve(String.valueOf(routeId)))).isFalse();
    }

    private long createRoute(RouteStatus status) {
        Route route = new Route(
                "Ruta de fotos " + UUID.randomUUID(),
                "Ruta de prueba para la galería",
                "Madrid",
                "Navacerrada",
                LocalDate.now().plusDays(5),
                45.0,
                Difficulty.EASY,
                20,
                status
        );
        route.setMeetingTime(LocalTime.of(8, 0));
        route.setDepartureTime(LocalTime.of(8, 30));
        long routeId = routeRepository.save(route).getId();
        createdRouteIds.add(routeId);
        return routeId;
    }

    private void register(long routeId, User user) {
        RouteRegistration registration = new RouteRegistration();
        registration.setRoute(routeRepository.findById(routeId).orElseThrow());
        registration.setUser(user);
        registration.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(registration);
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@guatequeriders.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private org.springframework.test.web.servlet.ResultActions upload(
            String token, long routeId, String filename, String contentType, byte[] content) throws Exception {
        var request = multipart("/api/routes/{routeId}/photos", routeId)
                .file(new MockMultipartFile("file", filename, contentType, content));
        if (token != null) request.header("Authorization", bearer(token));
        return mockMvc.perform(request);
    }

    private String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", username, "password", password));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void deleteDirectoryIfEmpty(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (var paths = Files.list(directory)) {
                if (!paths.findAny().isPresent()) Files.deleteIfExists(directory);
            }
        }
    }
}
