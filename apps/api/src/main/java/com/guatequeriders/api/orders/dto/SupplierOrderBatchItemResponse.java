package com.guatequeriders.api.orders.dto;

import com.guatequeriders.api.orders.model.SupplierOrderBatchItem;

import java.math.BigDecimal;

public record SupplierOrderBatchItemResponse(
        String productCode,
        String productName,
        String size,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String customName
) {
    public static SupplierOrderBatchItemResponse from(SupplierOrderBatchItem item) {
        return new SupplierOrderBatchItemResponse(
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
