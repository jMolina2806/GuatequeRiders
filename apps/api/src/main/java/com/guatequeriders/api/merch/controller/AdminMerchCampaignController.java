package com.guatequeriders.api.merch.controller;

import com.guatequeriders.api.merch.dto.CampaignCloseResponse;
import com.guatequeriders.api.merch.dto.CampaignDashboardResponse;
import com.guatequeriders.api.merch.dto.CampaignHistoryEntryResponse;
import com.guatequeriders.api.merch.dto.CampaignOpeningReadinessResponse;
import com.guatequeriders.api.merch.dto.MerchCampaignResponse;
import com.guatequeriders.api.merch.service.MerchCampaignService;
import com.guatequeriders.api.orders.dto.SupplierOrderBatchResponse;
import com.guatequeriders.api.users.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/merch/campaigns")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMerchCampaignController {

    private final MerchCampaignService campaignService;

    public AdminMerchCampaignController(MerchCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    public ResponseEntity<List<CampaignHistoryEntryResponse>> listCampaigns() {
        return ResponseEntity.ok(campaignService.listCampaigns());
    }

    @GetMapping("/readiness")
    public ResponseEntity<CampaignOpeningReadinessResponse> openingReadiness() {
        return ResponseEntity.ok(campaignService.openingReadiness());
    }

    @PostMapping
    public ResponseEntity<MerchCampaignResponse> openCampaign(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MerchCampaignResponse.from(campaignService.openCampaign(user)));
    }

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<CampaignDashboardResponse> getDashboard(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getDashboard(id));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<CampaignCloseResponse> close(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(campaignService.close(id, user));
    }

    @GetMapping("/{id}/supplier-order")
    public ResponseEntity<SupplierOrderBatchResponse> getSupplierOrder(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getSupplierOrder(id));
    }

    @PostMapping("/{id}/ordered")
    public ResponseEntity<MerchCampaignResponse> markOrdered(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(MerchCampaignResponse.from(campaignService.markOrdered(id, user)));
    }
}
