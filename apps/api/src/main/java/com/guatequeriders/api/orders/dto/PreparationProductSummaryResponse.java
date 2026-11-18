package com.guatequeriders.api.orders.dto;

public record PreparationProductSummaryResponse(
        String productCode,
        String productName,
        int quantity
) {
}
