package com.guatequeriders.api.orders.dto;

public record DevelopmentPaymentStatusResponse(
        boolean enabled,
        String environment
) {
}
