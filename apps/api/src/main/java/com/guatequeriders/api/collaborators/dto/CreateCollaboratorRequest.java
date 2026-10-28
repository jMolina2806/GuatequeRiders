package com.guatequeriders.api.collaborators.dto;

import com.guatequeriders.api.collaborators.model.CollaboratorType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateCollaboratorRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name is too long")
    private String name;

    @NotNull(message = "Type is required")
    private CollaboratorType type;

    @NotBlank(message = "Short description is required")
    @Size(max = 500, message = "Short description is too long")
    private String shortDescription;

    @Min(value = 0, message = "Discount percentage must be greater than or equal to 0")
    @Max(value = 100, message = "Discount percentage must be less than or equal to 100")
    private Integer discountPercentage;

    @Size(max = 1000, message = "Benefit is too long")
    private String benefit;

    @Pattern(regexp = "^https://\\S+$", message = "Website URL must be a valid HTTPS URL")
    @Size(max = 1000, message = "Website URL is too long")
    private String websiteUrl;

    private Boolean featured;
    private Boolean active;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CollaboratorType getType() { return type; }
    public void setType(CollaboratorType type) { this.type = type; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public Integer getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Integer discountPercentage) { this.discountPercentage = discountPercentage; }

    public String getBenefit() { return benefit; }
    public void setBenefit(String benefit) { this.benefit = benefit; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
