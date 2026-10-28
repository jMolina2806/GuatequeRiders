package com.guatequeriders.api.merch.dto;

import com.guatequeriders.api.orders.dto.OrderResponse;
import com.guatequeriders.api.orders.dto.PreparationOrdersResponse;
import com.guatequeriders.api.orders.dto.SupplierOrderBatchResponse;

import java.util.List;

public record CampaignDashboardResponse(
        MerchCampaignResponse campaign,
        CampaignStatsResponse stats,
        PreparationOrdersResponse provisionalPreparation,
        SupplierOrderBatchResponse supplierOrder,
        List<OrderResponse> excludedOrders,
        List<OrderResponse> latePaymentOrders
) {
}
