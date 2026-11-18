package com.guatequeriders.api.events.dto;

import com.guatequeriders.api.events.model.Event;
import com.guatequeriders.api.events.model.EventStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private EventStatus status;
    private LocalDateTime createdAt;

    public static EventResponse from(Event event, long currentParticipants) {
        EventResponse r = new EventResponse();
        r.id = event.getId();
        r.title = event.getTitle();
        r.description = event.getDescription();
        r.location = event.getLocation();
        r.date = event.getDate();
        r.startTime = event.getStartTime();
        r.endTime = event.getEndTime();
        r.maxParticipants = event.getMaxParticipants();
        r.currentParticipants = (int) currentParticipants;
        r.status = event.getStatus();
        r.createdAt = event.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public Integer getCurrentParticipants() { return currentParticipants; }
    public EventStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
