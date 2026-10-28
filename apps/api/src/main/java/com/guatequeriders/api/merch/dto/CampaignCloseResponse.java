package com.guatequeriders.api.merch.dto;

import com.guatequeriders.api.orders.dto.SupplierOrderBatchResponse;

public record CampaignCloseResponse(
        MerchCampaignResponse campaign,
        SupplierOrderBatchResponse supplierOrder
) {
}
