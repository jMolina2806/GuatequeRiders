package com.guatequeriders.api.collaborators.dto;

import com.guatequeriders.api.collaborators.model.Collaborator;
import com.guatequeriders.api.collaborators.model.CollaboratorType;

import java.time.LocalDateTime;

public class CollaboratorResponse {

    private Long id;
    private String name;
    private CollaboratorType type;
    private String shortDescription;
    private Integer discountPercentage;
    private String benefit;
    private String websiteUrl;
    private String imageUrl;
    private boolean featured;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CollaboratorResponse from(Collaborator collaborator, String publicImageUrl) {
        CollaboratorResponse response = new CollaboratorResponse();
        response.id = collaborator.getId();
        response.name = collaborator.getName();
        response.type = collaborator.getType();
        response.shortDescription = collaborator.getShortDescription();
        response.discountPercentage = collaborator.getDiscountPercentage();
        response.benefit = collaborator.getBenefit();
        response.websiteUrl = collaborator.getWebsiteUrl();
        response.imageUrl = publicImageUrl;
        response.featured = collaborator.isFeatured();
        response.active = collaborator.isActive();
        response.createdAt = collaborator.getCreatedAt();
        response.updatedAt = collaborator.getUpdatedAt();
        return response;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public CollaboratorType getType() { return type; }
    public String getShortDescription() { return shortDescription; }
    public Integer getDiscountPercentage() { return discountPercentage; }
    public String getBenefit() { return benefit; }
    public String getWebsiteUrl() { return websiteUrl; }
    public String getImageUrl() { return imageUrl; }
    public boolean isFeatured() { return featured; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
