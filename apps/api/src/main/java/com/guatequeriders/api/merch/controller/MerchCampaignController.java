package com.guatequeriders.api.merch.controller;

import com.guatequeriders.api.merch.dto.MerchCampaignResponse;
import com.guatequeriders.api.merch.service.MerchCampaignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merch/campaign")
public class MerchCampaignController {

    private final MerchCampaignService campaignService;

    public MerchCampaignController(MerchCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping("/current")
    public ResponseEntity<MerchCampaignResponse> getCurrent() {
        return ResponseEntity.ok(MerchCampaignResponse.from(campaignService.getCurrent()));
    }
}
