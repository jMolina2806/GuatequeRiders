package com.guatequeriders.api.notifications.model;

import com.guatequeriders.api.users.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notification_preferences")
public class UserNotificationPreferences {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "merch_notifications", nullable = false)
    private boolean merchNotifications;

    @Column(name = "route_notifications", nullable = false)
    private boolean routeNotifications;

    @Column(name = "event_notifications", nullable = false)
    private boolean eventNotifications;

    @Column(name = "circuit_notifications", nullable = false)
    private boolean circuitNotifications;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getUserId() { return userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public boolean isMerchNotifications() { return merchNotifications; }
    public void setMerchNotifications(boolean merchNotifications) { this.merchNotifications = merchNotifications; }

    public boolean isRouteNotifications() { return routeNotifications; }
    public void setRouteNotifications(boolean routeNotifications) { this.routeNotifications = routeNotifications; }

    public boolean isEventNotifications() { return eventNotifications; }
    public void setEventNotifications(boolean eventNotifications) { this.eventNotifications = eventNotifications; }

    public boolean isCircuitNotifications() { return circuitNotifications; }
    public void setCircuitNotifications(boolean circuitNotifications) { this.circuitNotifications = circuitNotifications; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
