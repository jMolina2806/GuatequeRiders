package com.guatequeriders.api.orders.dto;

import com.guatequeriders.api.merch.model.ProductSize;

public record PreparationSummaryResponse(
        String productCode,
        String productName,
        ProductSize size,
        int quantity
) {
}
