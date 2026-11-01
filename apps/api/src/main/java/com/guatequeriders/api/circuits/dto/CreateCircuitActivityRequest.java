package com.guatequeriders.api.circuits.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class CreateCircuitActivityRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 160, message = "Title is too long")
    private String title;

    @NotBlank(message = "Circuit name is required")
    @Size(max = 255, message = "Circuit name is too long")
    private String circuitName;

    @NotBlank(message = "Description is required")
    @Size(max = 4000, message = "Description is too long")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location is too long")
    private String location;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    private LocalTime endTime;

    @DecimalMin(value = "0.0", message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @Size(max = 255, message = "Discount is too long")
    private String discount;

    @Size(max = 2000, message = "Benefit is too long")
    private String benefit;

    @NotBlank(message = "Registration URL is required")
    @Size(max = 1000, message = "Registration URL is too long")
    @Pattern(regexp = "^https://\\S+$", message = "Registration URL must be a valid HTTPS URL")
    private String registrationUrl;

    @Pattern(regexp = "^https://\\S+$", message = "Information URL must be a valid HTTPS URL")
    @Size(max = 1000, message = "Information URL is too long")
    private String informationUrl;

    @Pattern(regexp = "^https://\\S+$", message = "Image URL must be a valid HTTPS URL")
    @Size(max = 1000, message = "Image URL is too long")
    private String imageUrl;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCircuitName() { return circuitName; }
    public void setCircuitName(String circuitName) { this.circuitName = circuitName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDiscount() { return discount; }
    public void setDiscount(String discount) { this.discount = discount; }

    public String getBenefit() { return benefit; }
    public void setBenefit(String benefit) { this.benefit = benefit; }

    public String getRegistrationUrl() { return registrationUrl; }
    public void setRegistrationUrl(String registrationUrl) { this.registrationUrl = registrationUrl; }

    public String getInformationUrl() { return informationUrl; }
    public void setInformationUrl(String informationUrl) { this.informationUrl = informationUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
