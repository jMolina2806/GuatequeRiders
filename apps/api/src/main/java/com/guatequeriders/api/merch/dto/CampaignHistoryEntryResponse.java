package com.guatequeriders.api.merch.dto;

public record CampaignHistoryEntryResponse(
        MerchCampaignResponse campaign,
        int totalOrders,
        int paidOrders,
        int pendingOrders,
        int totalUnits
) {
}
