package com.guatequeriders.api.payments.service;

import java.util.List;

public record CheckoutPreparation(
        Long paymentAttemptId,
        Long orderId,
        Long campaignId,
        String idempotencyKey,
        String sessionId,
        String checkoutUrl,
        long amountMinor,
        String currency,
        String customerEmail,
        List<LineItem> lineItems
) {

    public boolean hasSession() {
        return sessionId != null && !sessionId.isBlank();
    }

    public boolean hasReusableUrl() {
        return hasSession() && checkoutUrl != null && !checkoutUrl.isBlank();
    }

    public record LineItem(
            String name,
            String description,
            long unitAmountMinor,
            long quantity
    ) {
    }
}
