package com.guatequeriders.api.admin.dto;

public record AdminStatsResponse(
        long totalUsers,
        long totalRoutes,
        long publishedRoutes,
        long totalRouteRegistrations,
        long totalEvents,
        long publishedEvents,
        long totalEventRegistrations
) {
}
