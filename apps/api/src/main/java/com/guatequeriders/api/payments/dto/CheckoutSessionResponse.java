package com.guatequeriders.api.payments.dto;

public record CheckoutSessionResponse(
        String checkoutUrl,
        String sessionId
) {
}
