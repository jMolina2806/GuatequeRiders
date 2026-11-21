package com.guatequeriders.api.news;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/news")
public class NewsController {
    private final NewsRepository repository;
    private final ObjectMapper mapper;
    private final Path uploadRoot;

    public NewsController(NewsRepository repository, ObjectMapper mapper,
                          @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.repository = repository;
        this.mapper = mapper;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("news");
    }

    public record Media(String url, String type) {}
    public record NewsRequest(String title, String summary, String body, String category,
                              List<Media> media, boolean published, boolean featured, OffsetDateTime publishAt) {}
    public record NewsResponse(Long id, String title, String summary, String body, String category,
                               List<Media> media, boolean published, boolean featured,
                               OffsetDateTime publishAt, OffsetDateTime createdAt) {}

    private List<Media> media(News news) {
        try {
            return mapper.readValue(news.getMediaJson(), new TypeReference<List<Media>>() {});
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid media data");
        }
    }

    private NewsResponse response(News news) {
        return new NewsResponse(news.getId(), news.getTitle(), news.getSummary(), news.getBody(),
                news.getCategory(), media(news), news.isPublished(), news.isFeatured(),
                news.getPublishAt(), news.getCreatedAt());
    }

    private boolean visible(News news) {
        return news.isPublished() && (news.getPublishAt() == null || !news.getPublishAt().isAfter(OffsetDateTime.now()));
    }

    @GetMapping
    public List<NewsResponse> publicNews() {
        return repository.findAllByOrderByCreatedAtDesc().stream().filter(this::visible)
                .sorted(Comparator.comparing(News::isFeatured).reversed()
                        .thenComparing(News::getCreatedAt, Comparator.reverseOrder()))
                .map(this::response).toList();
    }

    @GetMapping("/{id}")
    public NewsResponse publicOne(@PathVariable Long id) {
        News news = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!visible(news)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return response(news);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<NewsResponse> adminNews() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::response).toList();
    }

    private void apply(News news, NewsRequest request) {
        if (request.title() == null || request.title().isBlank() || request.title().length() > 180
                || request.summary() == null || request.summary().isBlank() || request.summary().length() > 500
                || request.body() == null || request.body().isBlank() || request.body().length() > 20000
                || request.category() == null || !Set.of("COLLABORATORS", "COMMUNITY", "ROUTES", "MULTIMEDIA", "PROMOTIONS").contains(request.category())
                || request.media() != null && request.media().size() > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid news content");
        }
        List<Media> items = request.media() == null ? List.of() : request.media();
        for (Media item : items) {
            if (item == null || item.url() == null || item.type() == null ||
                    !Set.of("IMAGE", "VIDEO").contains(item.type()) ||
                    !(item.url().matches("^/api/news/media/[a-f0-9-]{36}\\.(jpg|png|webp|mp4|webm)$")
                      || item.type().equals("VIDEO") && item.url().matches("^https://(www\\.)?(youtube\\.com/watch\\?v=[A-Za-z0-9_-]{11}|youtu\\.be/[A-Za-z0-9_-]{11})$"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid media URL");
            }
        }
        try { news.setMediaJson(mapper.writeValueAsString(items)); }
        catch (IOException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid media"); }
        news.setTitle(request.title().trim());
        news.setSummary(request.summary().trim());
        news.setBody(request.body().trim());
        news.setCategory(request.category());
        news.setPublished(request.published());
        news.setFeatured(request.featured());
        news.setPublishAt(request.publishAt());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public NewsResponse create(@RequestBody NewsRequest request) {
        News news = new News();
        apply(news, request);
        return response(repository.save(news));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public NewsResponse update(@PathVariable Long id, @RequestBody NewsRequest request) {
        News news = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        apply(news, request);
        return response(repository.save(news));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        repository.deleteById(id);
    }

    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Media upload(@RequestParam("file") MultipartFile file) throws IOException {
        String type = file.getContentType();
        String ext;
        String kind;
        if ("image/jpeg".equals(type)) { ext = "jpg"; kind = "IMAGE"; }
        else if ("image/png".equals(type)) { ext = "png"; kind = "IMAGE"; }
        else if ("image/webp".equals(type)) { ext = "webp"; kind = "IMAGE"; }
        else if ("video/mp4".equals(type)) { ext = "mp4"; kind = "VIDEO"; }
        else if ("video/webm".equals(type)) { ext = "webm"; kind = "VIDEO"; }
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported media type");
        if (file.isEmpty() || file.getSize() > 25L * 1024 * 1024)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum file size is 25 MB");
        Files.createDirectories(uploadRoot);
        String filename = UUID.randomUUID() + "." + ext;
        Path target = uploadRoot.resolve(filename);
        try (var stream = file.getInputStream()) { Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING); }
        return new Media("/api/news/media/" + filename, kind);
    }

    @GetMapping("/media/{filename}")
    public ResponseEntity<byte[]> file(@PathVariable String filename) throws IOException {
        if (!filename.matches("[a-f0-9-]{36}\\.(jpg|png|webp|mp4|webm)"))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        String url = "/api/news/media/" + filename;
        boolean referenced = repository.findAll().stream().filter(this::visible)
                .anyMatch(news -> media(news).stream().anyMatch(item -> url.equals(item.url())));
        if (!referenced) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Path path = uploadRoot.resolve(filename).normalize();
        if (!path.startsWith(uploadRoot) || !Files.isRegularFile(path))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        MediaType contentType = filename.endsWith(".mp4") ? MediaType.valueOf("video/mp4")
                : filename.endsWith(".webm") ? MediaType.valueOf("video/webm")
                : filename.endsWith(".png") ? MediaType.IMAGE_PNG
                : filename.endsWith(".webp") ? MediaType.valueOf("image/webp") : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(contentType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(Files.readAllBytes(path));
    }
}
