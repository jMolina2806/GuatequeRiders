package com.guatequeriders.api.orders.controller;

import com.guatequeriders.api.orders.dto.OrderResponse;
import com.guatequeriders.api.orders.service.OrdersService;
import com.guatequeriders.api.users.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/me")
public class UserOrdersController {

    private final OrdersService ordersService;

    public UserOrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ordersService.findForUser(user).stream()
                .map(OrderResponse::from)
                .toList());
    }
}
