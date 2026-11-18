package com.guatequeriders.api.circuits.dto;

import com.guatequeriders.api.circuits.model.CircuitActivity;
import com.guatequeriders.api.circuits.model.CircuitActivityStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CircuitActivityResponse {

    private Long id;
    private String title;
    private String circuitName;
    private String description;
    private String location;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private String discount;
    private String benefit;
    private String registrationUrl;
    private String informationUrl;
    private String imageUrl;
    private CircuitActivityStatus status;
    private LocalDateTime createdAt;

    public static CircuitActivityResponse from(CircuitActivity activity) {
        CircuitActivityResponse response = new CircuitActivityResponse();
        response.id = activity.getId();
        response.title = activity.getTitle();
        response.circuitName = activity.getCircuitName();
        response.description = activity.getDescription();
        response.location = activity.getLocation();
        response.date = activity.getDate();
        response.startTime = activity.getStartTime();
        response.endTime = activity.getEndTime();
        response.price = activity.getPrice();
        response.discount = activity.getDiscount();
        response.benefit = activity.getBenefit();
        response.registrationUrl = activity.getRegistrationUrl();
        response.informationUrl = activity.getInformationUrl();
        response.imageUrl = activity.getImageUrl();
        response.status = activity.getStatus();
        response.createdAt = activity.getCreatedAt();
        return response;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCircuitName() { return circuitName; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public BigDecimal getPrice() { return price; }
    public String getDiscount() { return discount; }
    public String getBenefit() { return benefit; }
    public String getRegistrationUrl() { return registrationUrl; }
    public String getInformationUrl() { return informationUrl; }
    public String getImageUrl() { return imageUrl; }
    public CircuitActivityStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
