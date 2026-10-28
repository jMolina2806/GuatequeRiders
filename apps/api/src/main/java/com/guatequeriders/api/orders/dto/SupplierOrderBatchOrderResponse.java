package com.guatequeriders.api.orders.dto;

import com.guatequeriders.api.orders.model.SupplierOrderBatchOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SupplierOrderBatchOrderResponse(
        Long orderId,
        String customerName,
        LocalDateTime orderCreatedAt,
        BigDecimal total,
        List<SupplierOrderBatchItemResponse> items
) {
    public static SupplierOrderBatchOrderResponse from(SupplierOrderBatchOrder order) {
        return new SupplierOrderBatchOrderResponse(
                order.getOriginalOrderId(),
                order.getCustomerName(),
                order.getOrderCreatedAt(),
                order.getTotal(),
                order.getItems().stream().map(SupplierOrderBatchItemResponse::from).toList()
        );
    }
}
