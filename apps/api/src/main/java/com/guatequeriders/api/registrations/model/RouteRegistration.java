package com.guatequeriders.api.registrations.model;

import java.time.LocalDateTime;

public class RouteRegistration {

    private Long id;
    private Long userId;
    private Long routeId;
    private LocalDateTime registeredAt;

    public RouteRegistration() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}
