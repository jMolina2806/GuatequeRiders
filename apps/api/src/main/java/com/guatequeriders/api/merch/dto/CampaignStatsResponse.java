package com.guatequeriders.api.merch.dto;

public record CampaignStatsResponse(
        int totalOrders,
        int paidOrders,
        int pendingOrders,
        int failedOrders,
        int cancelledOrders,
        int paidUnits
) {
}
