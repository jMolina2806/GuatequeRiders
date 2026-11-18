package com.guatequeriders.api.routes.dto;

import com.guatequeriders.api.routes.model.Difficulty;
import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.model.RouteStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class RouteResponse {

    private Long id;
    private String title;
    private String description;
    private String origin;
    private String destination;
    private LocalDate date;
    private LocalTime meetingTime;
    private LocalTime departureTime;
    private Double distanceKm;
    private Difficulty difficulty;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private RouteStatus status;
    private List<RouteOrganizerResponse> organizers;

    public static RouteResponse from(Route route, long currentParticipants,
                                     List<RouteOrganizerResponse> organizers) {
        RouteResponse r = new RouteResponse();
        r.id = route.getId();
        r.title = route.getTitle();
        r.description = route.getDescription();
        r.origin = route.getOrigin();
        r.destination = route.getDestination();
        r.date = route.getDate();
        r.meetingTime = route.getMeetingTime();
        r.departureTime = route.getDepartureTime();
        r.distanceKm = route.getDistanceKm();
        r.difficulty = route.getDifficulty();
        r.maxParticipants = route.getMaxParticipants();
        r.currentParticipants = (int) currentParticipants;
        r.status = route.getStatus();
        r.organizers = organizers;
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDate getDate() { return date; }
    public LocalTime getMeetingTime() { return meetingTime; }
    public LocalTime getDepartureTime() { return departureTime; }
    public Double getDistanceKm() { return distanceKm; }
    public Difficulty getDifficulty() { return difficulty; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public Integer getCurrentParticipants() { return currentParticipants; }
    public RouteStatus getStatus() { return status; }
    public List<RouteOrganizerResponse> getOrganizers() { return organizers; }
}
