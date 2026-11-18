package com.guatequeriders.api.routes.photos.service;

import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.config.UploadProperties;
import com.guatequeriders.api.routes.photos.model.RoutePhoto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class RoutePhotoStorageService {

    public static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final Logger log = LoggerFactory.getLogger(RoutePhotoStorageService.class);
    private static final Pattern MANAGED_FILENAME = Pattern.compile(
            "^[0-9a-fA-F-]{36}\\.(png|jpg|webp)$");

    private final Path routesRoot;

    public RoutePhotoStorageService(UploadProperties uploadProperties) {
        this.routesRoot = uploadProperties.getRoutesRoot().toAbsolutePath().normalize();
    }

    public StoredPhoto store(Long routeId, MultipartFile file) {
        ValidatedImage image = validateAndRead(file);
        Path routeDirectory = routeDirectory(routeId);
        String storedFilename = UUID.randomUUID() + image.format().extension();
        Path target = routeDirectory.resolve(storedFilename).normalize();
        ensureInsideRouteDirectory(target, routeDirectory);
        Path temporary = null;

        try {
            Files.createDirectories(routeDirectory);
            temporary = Files.createTempFile(routeDirectory, "upload-", ".tmp");
            Files.write(temporary, image.bytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            moveIntoPlace(temporary, target);
            temporary = null;
            return new StoredPhoto(
                    storedFilename,
                    image.format().mediaType(),
                    image.bytes().length,
                    sanitizeOriginalName(file.getOriginalFilename())
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store route photo", exception);
        } finally {
            if (temporary != null) {
                deleteQuietly(temporary);
            }
        }
    }

    public ServedPhoto load(RoutePhoto photo) {
        Path path = resolveExistingFile(photo.getRoute().getId(), photo.getStoredFilename());
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(photo.getContentType());
        } catch (IllegalArgumentException exception) {
            throw new ResourceNotFoundException("Route photo not found");
        }
        return new ServedPhoto(
                new FileSystemResource(path),
                mediaType,
                downloadFilename(photo),
                photo.getSizeBytes()
        );
    }

    public void delete(Long routeId, String storedFilename) {
        Path path = resolveManagedFile(routeId, storedFilename);
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            log.error("Could not delete route photo file routeId={} filename={}",
                    routeId, storedFilename, exception);
        }
    }

    public void deleteRouteDirectory(Long routeId) {
        Path directory = routeDirectory(routeId);
        if (!Files.exists(directory)) return;
        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(this::deletePathOrLog);
        } catch (IOException exception) {
            log.error("Could not clean route photo directory routeId={}", routeId, exception);
        }
    }

    private ValidatedImage validateAndRead(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image file must not exceed 5 MB");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read image file", exception);
        }
        if (bytes.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image file must not exceed 5 MB");
        }
        ImageFormat detected = ImageFormat.detect(bytes);
        String declaredMime = normalizeMime(file.getContentType());
        if (detected == null || !detected.mediaType().toString().equals(declaredMime)) {
            throw new IllegalArgumentException("Image MIME type does not match its content");
        }
        return new ValidatedImage(bytes, detected);
    }

    private String normalizeMime(String contentType) {
        if (contentType == null) return "";
        return contentType.toLowerCase(Locale.ROOT).split(";", 2)[0].trim();
    }

    private String sanitizeOriginalName(String originalName) {
        String value = originalName == null ? "" : originalName.replace('\\', '/');
        int lastSlash = value.lastIndexOf('/');
        if (lastSlash >= 0) value = value.substring(lastSlash + 1);
        value = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replaceAll("[\\p{Cntrl}]", "")
                .replaceAll("[^A-Za-z0-9._-]", "_")
                .replaceAll("^\\.+", "")
                .replaceAll("\\.+$", "");
        if (value.isBlank()) return null;
        if (value.length() > 120) value = value.substring(0, 120);
        return value;
    }

    private String downloadFilename(RoutePhoto photo) {
        return "guatequeriders-route-" + photo.getRoute().getId()
                + "-photo-" + photo.getId() + extensionOf(photo.getStoredFilename());
    }

    private String extensionOf(String filename) {
        int dot = filename == null ? -1 : filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }

    private Path routeDirectory(Long routeId) {
        if (routeId == null || routeId < 1) {
            throw new IllegalArgumentException("Invalid route id");
        }
        Path directory = routesRoot.resolve(String.valueOf(routeId)).normalize();
        if (!directory.startsWith(routesRoot) || !routesRoot.equals(directory.getParent())) {
            throw new IllegalArgumentException("Invalid route photo path");
        }
        return directory;
    }

    private Path resolveManagedFile(Long routeId, String storedFilename) {
        if (storedFilename == null || !MANAGED_FILENAME.matcher(storedFilename).matches()) return null;
        Path directory = routeDirectory(routeId);
        Path path = directory.resolve(storedFilename).normalize();
        ensureInsideRouteDirectory(path, directory);
        return path;
    }

    private Path resolveExistingFile(Long routeId, String storedFilename) {
        Path path = resolveManagedFile(routeId, storedFilename);
        if (path == null || !Files.isRegularFile(path)) {
            throw new ResourceNotFoundException("Route photo not found");
        }
        return path;
    }

    private void ensureInsideRouteDirectory(Path path, Path directory) {
        if (!path.startsWith(directory) || !directory.equals(path.getParent())) {
            throw new IllegalArgumentException("Invalid route photo path");
        }
    }

    private void moveIntoPlace(Path temporary, Path target) throws IOException {
        try {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            log.warn("Could not clean temporary route photo file path={}", path);
        }
    }

    private void deletePathOrLog(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            log.error("Could not delete route photo path={}", path, exception);
        }
    }

    public record StoredPhoto(String storedFilename, MediaType mediaType, long sizeBytes, String originalName) {}

    public record ServedPhoto(Resource resource, MediaType mediaType,
                               String downloadFilename, long sizeBytes) {}

    private record ValidatedImage(byte[] bytes, ImageFormat format) {}

    private enum ImageFormat {
        PNG(".png", MediaType.IMAGE_PNG),
        JPEG(".jpg", MediaType.IMAGE_JPEG),
        WEBP(".webp", MediaType.parseMediaType("image/webp"));

        private final String extension;
        private final MediaType mediaType;

        ImageFormat(String extension, MediaType mediaType) {
            this.extension = extension;
            this.mediaType = mediaType;
        }

        String extension() { return extension; }
        MediaType mediaType() { return mediaType; }

        static ImageFormat detect(byte[] bytes) {
            if (bytes.length >= 8
                    && (bytes[0] & 0xFF) == 0x89
                    && bytes[1] == 0x50
                    && bytes[2] == 0x4E
                    && bytes[3] == 0x47
                    && bytes[4] == 0x0D
                    && bytes[5] == 0x0A
                    && bytes[6] == 0x1A
                    && bytes[7] == 0x0A) {
                return PNG;
            }
            if (bytes.length >= 3
                    && (bytes[0] & 0xFF) == 0xFF
                    && (bytes[1] & 0xFF) == 0xD8
                    && (bytes[2] & 0xFF) == 0xFF) {
                return JPEG;
            }
            if (bytes.length >= 12
                    && bytes[0] == 'R'
                    && bytes[1] == 'I'
                    && bytes[2] == 'F'
                    && bytes[3] == 'F'
                    && bytes[8] == 'W'
                    && bytes[9] == 'E'
                    && bytes[10] == 'B'
                    && bytes[11] == 'P') {
                return WEBP;
            }
            return null;
        }
    }
}
