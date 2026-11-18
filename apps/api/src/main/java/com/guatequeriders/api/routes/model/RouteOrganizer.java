package com.guatequeriders.api.routes.model;

import com.guatequeriders.api.users.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "route_organizers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_route_organizers_route_user",
                columnNames = {"route_id", "user_id"}
        )
)
public class RouteOrganizer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public RouteOrganizer() {
    }

    public RouteOrganizer(Route route, User user) {
        this.route = route;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
