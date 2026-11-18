package com.guatequeriders.api.orders.controller;

import com.guatequeriders.api.orders.dto.CreateOrderRequest;
import com.guatequeriders.api.orders.dto.OrderResponse;
import com.guatequeriders.api.orders.service.OrdersService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guatequeriders.api.users.model.User;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final OrdersService ordersService;

    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request,
                                                @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderResponse.from(ordersService.create(request, user)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id,
                                                 @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(OrderResponse.from(ordersService.getForUser(id, user)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id,
                                                @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(OrderResponse.from(ordersService.cancel(id, user)));
    }
}
