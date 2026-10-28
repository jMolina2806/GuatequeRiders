package com.guatequeriders.api.merch.dto;

import com.guatequeriders.api.merch.model.MerchCampaign;
import com.guatequeriders.api.merch.model.CampaignStatus;

import java.time.LocalDateTime;

public record MerchCampaignResponse(
        Long id,
        CampaignStatus status,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        LocalDateTime orderedAt,
        boolean acceptingOrders,
        boolean acceptingPayments
) {
    public static MerchCampaignResponse from(MerchCampaign campaign) {
        return new MerchCampaignResponse(
                campaign.getId(),
                campaign.getStatus(),
                campaign.getOpenedAt(),
                campaign.getClosedAt(),
                campaign.getOrderedAt(),
                campaign.acceptsOrders(),
                campaign.acceptsPayments()
        );
    }
}
