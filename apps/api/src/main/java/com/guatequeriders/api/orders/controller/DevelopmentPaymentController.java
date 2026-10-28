package com.guatequeriders.api.orders.controller;

import com.guatequeriders.api.orders.dto.DevelopmentPaymentStatusResponse;
import com.guatequeriders.api.orders.dto.OrderResponse;
import com.guatequeriders.api.orders.service.OrdersService;
import com.guatequeriders.api.users.model.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@ConditionalOnProperty(prefix = "app.merch", name = "test-payments-enabled", havingValue = "true")
@ConditionalOnExpression("'${app.environment:production}' == 'development'")
@RestController
@RequestMapping("/api/dev")
public class DevelopmentPaymentController {

    private final OrdersService ordersService;

    public DevelopmentPaymentController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @GetMapping("/payments/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DevelopmentPaymentStatusResponse> status() {
        return ResponseEntity.ok(new DevelopmentPaymentStatusResponse(true, "development"));
    }

    @PostMapping("/orders/{id}/simulate-payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> simulatePayment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(OrderResponse.from(ordersService.simulateDevelopmentPayment(id, user)));
    }
}
