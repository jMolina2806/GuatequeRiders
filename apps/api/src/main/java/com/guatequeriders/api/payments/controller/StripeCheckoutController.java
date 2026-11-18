package com.guatequeriders.api.payments.controller;

import com.guatequeriders.api.payments.dto.CheckoutSessionResponse;
import com.guatequeriders.api.payments.service.StripeCheckoutService;
import com.guatequeriders.api.users.model.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "true")
@RequestMapping("/api/orders")
public class StripeCheckoutController {

    private final StripeCheckoutService stripeCheckoutService;

    public StripeCheckoutController(StripeCheckoutService stripeCheckoutService) {
        this.stripeCheckoutService = stripeCheckoutService;
    }

    @PostMapping("/{id}/checkout-session")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(stripeCheckoutService.createCheckoutSession(id, user));
    }
}
