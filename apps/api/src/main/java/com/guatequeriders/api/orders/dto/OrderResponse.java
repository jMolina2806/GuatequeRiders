package com.guatequeriders.api.orders.dto;

import com.guatequeriders.api.orders.model.Order;
import com.guatequeriders.api.orders.model.OrderStatus;
import com.guatequeriders.api.orders.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        String username,
        Long campaignId,
        OrderStatus status,
        PaymentStatus paymentStatus,
        BigDecimal total,
        String customerName,
        String customerEmail,
        String customerPhone,
        String notes,
        String fulfillmentMethod,
        String address,
        String city,
        String postalCode,
        String province,
        LocalDateTime paidAt,
        boolean latePayment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getUsername(),
                order.getCampaign().getId(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getTotal(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerPhone(),
                order.getNotes(),
                order.getFulfillmentMethod(),
                order.getAddress(),
                order.getCity(),
                order.getPostalCode(),
                order.getProvince(),
                order.getPaidAt(),
                order.isLatePayment(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}
