package com.guatequeriders.api.routes.model;

import java.time.LocalDate;

public class Route {

    private Long id;
    private String title;
    private String description;
    private String origin;
    private String destination;
    private LocalDate date;
    private Double distanceKm;
    private Difficulty difficulty;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private RouteStatus status;

    public Route() {}

    public Route(Long id, String title, String description, String origin, String destination,
                 LocalDate date, Double distanceKm, Difficulty difficulty,
                 Integer maxParticipants, Integer currentParticipants, RouteStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.origin = origin;
        this.destination = destination;
        this.date = date;
        this.distanceKm = distanceKm;
        this.difficulty = difficulty;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = currentParticipants;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public Integer getCurrentParticipants() { return currentParticipants; }
    public void setCurrentParticipants(Integer currentParticipants) { this.currentParticipants = currentParticipants; }

    public RouteStatus getStatus() { return status; }
    public void setStatus(RouteStatus status) { this.status = status; }
}
