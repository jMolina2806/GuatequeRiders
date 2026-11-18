package com.guatequeriders.api.merch.dto;

import java.util.List;

public record CampaignOpeningReadinessResponse(
        boolean canOpen,
        List<String> blockers
) {
}
