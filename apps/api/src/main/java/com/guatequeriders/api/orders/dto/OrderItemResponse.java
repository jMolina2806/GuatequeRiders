package com.guatequeriders.api.orders.dto;

import com.guatequeriders.api.orders.model.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        String productCode,
        String productName,
        String size,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String customName
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductCode(),
                item.getProductName(),
                item.getSize(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal(),
                item.getCustomName()
        );
    }
}
