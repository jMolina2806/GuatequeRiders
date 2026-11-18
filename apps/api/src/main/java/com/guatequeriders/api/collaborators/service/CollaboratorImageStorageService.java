package com.guatequeriders.api.collaborators.service;

import com.guatequeriders.api.collaborators.model.CollaboratorType;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.config.UploadProperties;
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
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class CollaboratorImageStorageService {

    public static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final String RELATIVE_PREFIX = "collaborators/";
    private static final Pattern MANAGED_FILENAME = Pattern.compile(
            "^[0-9a-fA-F-]{36}\\.(png|jpg|webp)$");

    private final Path collaboratorsRoot;

    public CollaboratorImageStorageService(UploadProperties uploadProperties) {
        this.collaboratorsRoot = uploadProperties.getCollaboratorsRoot().toAbsolutePath().normalize();
    }

    public StoredImage store(MultipartFile file) {
        ImageFormat format = validateAndDetect(file);
        String filename = UUID.randomUUID() + format.extension();
        Path target = collaboratorsRoot.resolve(filename).normalize();
        ensureInsideCollaboratorsRoot(target);
        Path temporary = null;

        try {
            Files.createDirectories(collaboratorsRoot);
            temporary = Files.createTempFile(collaboratorsRoot, "upload-", ".tmp");
            Files.write(temporary, file.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            moveIntoPlace(temporary, target);
            temporary = null;
            return new StoredImage(RELATIVE_PREFIX + filename, format.mediaType());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store collaborator image", exception);
        } finally {
            if (temporary != null) {
                deleteQuietly(temporary);
            }
        }
    }

    public String publicUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return null;
        if (resolveManagedPath(relativePath) == null) return null;
        return "/api/uploads/" + relativePath.replace('\\', '/');
    }

    public void delete(String relativePath) {
        Path path = resolveManagedPath(relativePath);
        if (path == null) return;
        deleteQuietly(path);
    }

    public ServedImage load(String filename) {
        Path path = resolveManagedFilename(filename);
        ImageFormat format = ImageFormat.fromExtension(filename);
        return new ServedImage(new FileSystemResource(path), format.mediaType());
    }

    private ImageFormat validateAndDetect(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image file must not exceed 5 MB");
        }

        String declaredMime = normalizeMime(file.getContentType());
        ImageFormat detected;
        try {
            detected = ImageFormat.detect(file.getBytes());
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read image file", exception);
        }
        if (detected == null || !detected.mediaType().toString().equals(declaredMime)) {
            throw new IllegalArgumentException("Image MIME type does not match its content");
        }
        return detected;
    }

    private String normalizeMime(String contentType) {
        if (contentType == null) return "";
        return contentType.toLowerCase(Locale.ROOT).split(";", 2)[0].trim();
    }

    private Path resolveManagedFilename(String filename) {
        if (filename == null || filename.contains("/") || filename.contains("\\")
                || !MANAGED_FILENAME.matcher(filename).matches()) {
            throw new ResourceNotFoundException("Collaborator image not found");
        }
        Path path = collaboratorsRoot.resolve(filename).normalize();
        ensureInsideCollaboratorsRoot(path);
        if (!Files.isRegularFile(path)) {
            throw new ResourceNotFoundException("Collaborator image not found");
        }
        return path;
    }

    private Path resolveManagedPath(String relativePath) {
        if (relativePath == null || !relativePath.startsWith(RELATIVE_PREFIX)) return null;
        String filename = relativePath.substring(RELATIVE_PREFIX.length());
        if (!MANAGED_FILENAME.matcher(filename).matches()) return null;
        Path path = collaboratorsRoot.resolve(filename).normalize();
        if (!path.startsWith(collaboratorsRoot) || !collaboratorsRoot.equals(path.getParent())) return null;
        return path;
    }

    private void ensureInsideCollaboratorsRoot(Path path) {
        if (!path.startsWith(collaboratorsRoot) || !collaboratorsRoot.equals(path.getParent())) {
            throw new IllegalArgumentException("Invalid image path");
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
            // Cleanup is best effort; database operations must remain consistent.
        }
    }

    public record StoredImage(String relativePath, MediaType mediaType) {}

    public record ServedImage(Resource resource, MediaType mediaType) {}

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

        static ImageFormat fromExtension(String filename) {
            if (filename.endsWith(".png")) return PNG;
            if (filename.endsWith(".jpg")) return JPEG;
            if (filename.endsWith(".webp")) return WEBP;
            throw new ResourceNotFoundException("Collaborator image not found");
        }

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
