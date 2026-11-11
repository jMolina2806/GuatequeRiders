package com.guatequeriders.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guatequeriders.api.collaborators.repository.CollaboratorRepository;
import com.guatequeriders.api.users.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:collaborators;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "app.upload-dir=target/test-uploads/collaborators"
})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class CollaboratorsIntegrationTest {

    private static final byte[] PNG = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};
    private static final Path TEST_UPLOAD_ROOT = Paths.get("target/test-uploads/collaborators")
            .toAbsolutePath().normalize();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanTestData() throws IOException {
        collaboratorRepository.deleteAll();
        if (!Files.exists(TEST_UPLOAD_ROOT)) return;
        try (Stream<Path> paths = Files.walk(TEST_UPLOAD_ROOT)) {
            paths.sorted((left, right) -> right.compareTo(left))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {
                            throw new IllegalStateException(exception);
                        }
                    });
        }
    }

    @Test
    void publicUsersOnlySeeActiveCollaboratorsAndAdminsSeeInactive() throws Exception {
        String adminToken = login("admin", "admin123");
        createCollaborator(adminToken, "Colaborador activo", true);
        long inactiveId = createCollaborator(adminToken, "Colaborador inactivo", false);

        String publicBody = mockMvc.perform(get("/api/collaborators"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode publicCollaborators = objectMapper.readTree(publicBody);
        assertThat(publicCollaborators.findValuesAsText("name")).contains("Colaborador activo")
                .doesNotContain("Colaborador inactivo");

        mockMvc.perform(get("/api/collaborators/{id}", inactiveId))
                .andExpect(status().isNotFound());

        String adminBody = mockMvc.perform(get("/api/collaborators")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(objectMapper.readTree(adminBody).findValuesAsText("name"))
                .contains("Colaborador inactivo");
    }

    @Test
    void collaboratorBenefitsAreVisibleOnlyToMembersAndAdmins() throws Exception {
        String adminToken = login("admin", "admin123");
        String userToken = login("demo", "demo123");
        createCollaborator(adminToken, "Descuento privado", true);

        mockMvc.perform(get("/api/collaborators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].discountPercentage").doesNotExist())
                .andExpect(jsonPath("$[0].benefit").doesNotExist());

        mockMvc.perform(get("/api/collaborators")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].discountPercentage").doesNotExist())
                .andExpect(jsonPath("$[0].benefit").doesNotExist());

        long demoId = userRepository.findByUsername("demo").orElseThrow().getId();
        mockMvc.perform(patch("/api/admin/users/{id}/membership", demoId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", true))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/collaborators")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].discountPercentage").value(10))
                .andExpect(jsonPath("$[0].benefit").value("Beneficio confirmado"));

        mockMvc.perform(get("/api/collaborators")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].discountPercentage").value(10))
                .andExpect(jsonPath("$[0].benefit").value("Beneficio confirmado"));
    }

    @Test
    void onlyAdminCanCreateUpdateToggleAndDelete() throws Exception {
        String adminToken = login("admin", "admin123");
        String userToken = login("demo", "demo123");
        String json = collaboratorJson("Colaborador", "WORKSHOP", 10, false, true);

        mockMvc.perform(post("/api/collaborators")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/collaborators")
                        .header("Authorization", bearer(userToken))
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        long id = createCollaborator(adminToken, "Colaborador", true);

        mockMvc.perform(put("/api/collaborators/{id}", id)
                        .header("Authorization", bearer(userToken))
                        .contentType(APPLICATION_JSON)
                        .content(collaboratorJson("Editado", "SHOP", 20, true, true)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/collaborators/{id}/active", id)
                        .header("Authorization", bearer(userToken))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isForbidden());

        upload(userToken, id, "logo.png", "image/png", PNG)
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/collaborators/{id}", id)
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content(collaboratorJson("Editado", "SHOP", 20, true, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Editado"))
                .andExpect(jsonPath("$.type").value("SHOP"));

        mockMvc.perform(patch("/api/collaborators/{id}/active", id)
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(delete("/api/collaborators/{id}", id)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/collaborators/{id}", id)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void discountPercentageMustBeBetweenZeroAndOneHundred() throws Exception {
        String adminToken = login("admin", "admin123");

        mockMvc.perform(post("/api/collaborators")
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content(collaboratorJson("Negativo", "SHOP", -1, false, true)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/collaborators")
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content(collaboratorJson("Excesivo", "SHOP", 101, false, true)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validImageFormatsAreStoredWithRelativePathsAndServedPublicly() throws Exception {
        String adminToken = login("admin", "admin123");
        long id = createCollaborator(adminToken, "Imagenes", true);

        String pngResponse = upload(adminToken, id, "logo.png", "image/png", PNG)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(startsWith("/api/uploads/collaborators/")))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String pngUrl = objectMapper.readTree(pngResponse).get("imageUrl").asText();
        String pngPath = collaboratorRepository.findById(id).orElseThrow().getImageUrl();
        assertThat(pngPath).startsWith("collaborators/").doesNotStartWith("/api/");

        mockMvc.perform(get(pngUrl))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));

        upload(adminToken, id, "logo.jpg", "image/jpeg", JPEG)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(startsWith("/api/uploads/collaborators/")));
        String jpegPath = collaboratorRepository.findById(id).orElseThrow().getImageUrl();
        assertThat(Files.exists(fileForRelativePath(pngPath))).isFalse();

        String webpResponse = upload(adminToken, id, "logo.webp", "image/webp", WEBP)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(objectMapper.readTree(webpResponse).get("imageUrl").asText())
                .endsWith(".webp");
        assertThat(Files.exists(fileForRelativePath(jpegPath))).isFalse();
    }

    @Test
    void imageMimeMustMatchItsBinaryContent() throws Exception {
        String adminToken = login("admin", "admin123");
        long id = createCollaborator(adminToken, "Validacion", true);

        upload(adminToken, id, "wrong.png", "image/png", JPEG)
                .andExpect(status().isBadRequest());

        upload(adminToken, id, "document.pdf", "application/pdf", PNG)
                .andExpect(status().isBadRequest());
    }

    @Test
    void imageLargerThanFiveMegabytesIsRejected() throws Exception {
        String adminToken = login("admin", "admin123");
        long id = createCollaborator(adminToken, "Tamano", true);
        byte[] oversized = new byte[5 * 1024 * 1024 + 1];

        upload(adminToken, id, "large.png", "image/png", oversized)
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deletingImageAndCollaboratorRemovesManagedFiles() throws Exception {
        String adminToken = login("admin", "admin123");
        long id = createCollaborator(adminToken, "Limpieza", true);

        upload(adminToken, id, "logo.png", "image/png", PNG).andExpect(status().isOk());
        String firstPath = collaboratorRepository.findById(id).orElseThrow().getImageUrl();
        mockMvc.perform(delete("/api/collaborators/{id}/image", id)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").doesNotExist());
        assertThat(Files.exists(fileForRelativePath(firstPath))).isFalse();

        upload(adminToken, id, "logo.png", "image/png", PNG).andExpect(status().isOk());
        String secondPath = collaboratorRepository.findById(id).orElseThrow().getImageUrl();
        mockMvc.perform(delete("/api/collaborators/{id}", id)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());
        assertThat(Files.exists(fileForRelativePath(secondPath))).isFalse();
    }

    private long createCollaborator(String token, String name, boolean active) throws Exception {
        String response = mockMvc.perform(post("/api/collaborators")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(collaboratorJson(name, "WORKSHOP", 10, false, active)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions upload(
            String token, long id, String filename, String contentType, byte[] content) throws Exception {
        return mockMvc.perform(multipart("/api/collaborators/{id}/image", id)
                .file(new MockMultipartFile("file", filename, contentType, content))
                .header("Authorization", bearer(token)));
    }

    private Path fileForRelativePath(String relativePath) {
        assertThat(relativePath).startsWith("collaborators/");
        return TEST_UPLOAD_ROOT.resolve(relativePath.substring("collaborators/".length())).normalize();
    }

    private String collaboratorJson(String name, String type, Integer discount,
                                    boolean featured, boolean active) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("type", type);
        body.put("shortDescription", "Descripción del colaborador");
        body.put("discountPercentage", discount);
        body.put("benefit", "Beneficio confirmado");
        body.put("websiteUrl", "https://example.test/collaborator");
        body.put("featured", featured);
        body.put("active", active);
        return objectMapper.writeValueAsString(body);
    }

    private String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", username, "password", password));
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
