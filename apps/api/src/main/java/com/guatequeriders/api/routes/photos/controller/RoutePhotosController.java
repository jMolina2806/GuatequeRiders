package com.guatequeriders.api.routes.photos.controller;

import com.guatequeriders.api.routes.photos.dto.RoutePhotoResponse;
import com.guatequeriders.api.routes.photos.service.RoutePhotoStorageService;
import com.guatequeriders.api.routes.photos.service.RoutePhotosService;
import com.guatequeriders.api.users.model.User;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/routes/{routeId}/photos")
public class RoutePhotosController {

    private final RoutePhotosService photosService;

    public RoutePhotosController(RoutePhotosService photosService) {
        this.photosService = photosService;
    }

    @GetMapping
    public ResponseEntity<List<RoutePhotoResponse>> getAll(
            @PathVariable Long routeId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(photosService.findAll(routeId, isAdmin(user)));
    }

    @GetMapping("/{photoId}/file")
    public ResponseEntity<Resource> getFile(
            @PathVariable Long routeId,
            @PathVariable Long photoId,
            @AuthenticationPrincipal User user) {
        return serve(photosService.load(routeId, photoId, isAdmin(user)), false);
    }

    @GetMapping("/{photoId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long routeId,
            @PathVariable Long photoId,
            @AuthenticationPrincipal User user) {
        return serve(photosService.load(routeId, photoId, isAdmin(user)), true);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RoutePhotoResponse> upload(
            @PathVariable Long routeId,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(photosService.upload(routeId, file, user));
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long routeId,
            @PathVariable Long photoId,
            @AuthenticationPrincipal User user) {
        photosService.delete(routeId, photoId, user);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Resource> serve(RoutePhotoStorageService.ServedPhoto photo, boolean download) {
        ContentDisposition disposition = (download
                ? ContentDisposition.attachment()
                : ContentDisposition.inline())
                .filename(photo.downloadFilename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(photo.mediaType())
                .contentLength(photo.sizeBytes())
                .cacheControl(CacheControl.noCache().cachePrivate())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(photo.resource());
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole().name().equals("ADMIN");
    }
}
