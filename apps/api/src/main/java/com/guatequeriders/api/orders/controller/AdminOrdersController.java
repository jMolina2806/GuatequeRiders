package com.guatequeriders.api.orders.controller;

import com.guatequeriders.api.orders.dto.OrderResponse;
import com.guatequeriders.api.orders.service.OrdersService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrdersController {

    private final OrdersService ordersService;

    public AdminOrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @PostMapping("/{id}/advance-status")
    public ResponseEntity<OrderResponse> advanceStatus(@PathVariable Long id) {
        return ResponseEntity.ok(OrderResponse.from(ordersService.advanceFulfillmentStatus(id)));
    }
}
