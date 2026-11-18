package com.guatequeriders.api.routes.photos.dto;

import com.guatequeriders.api.routes.photos.model.RoutePhoto;

import java.time.LocalDateTime;

public class RoutePhotoResponse {

    private Long id;
    private String imageUrl;
    private String downloadUrl;
    private UploadedByResponse uploadedBy;
    private LocalDateTime createdAt;

    public static RoutePhotoResponse from(RoutePhoto photo) {
        RoutePhotoResponse response = new RoutePhotoResponse();
        Long routeId = photo.getRoute().getId();
        response.id = photo.getId();
        response.imageUrl = "/api/routes/" + routeId + "/photos/" + photo.getId() + "/file";
        response.downloadUrl = "/api/routes/" + routeId + "/photos/" + photo.getId() + "/download";
        response.uploadedBy = new UploadedByResponse(photo.getUser().getId(), photo.getUser().getUsername());
        response.createdAt = photo.getCreatedAt();
        return response;
    }

    public Long getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public String getDownloadUrl() { return downloadUrl; }
    public UploadedByResponse getUploadedBy() { return uploadedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public record UploadedByResponse(Long id, String username) {}
}
