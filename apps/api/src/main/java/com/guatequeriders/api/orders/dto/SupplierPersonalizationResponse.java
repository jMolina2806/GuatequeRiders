package com.guatequeriders.api.orders.dto;

import com.guatequeriders.api.orders.model.SupplierOrderBatchItem;
import com.guatequeriders.api.orders.model.SupplierOrderBatchOrder;

public record SupplierPersonalizationResponse(
        Long orderId,
        String productCode,
        String productName,
        String size,
        String customName,
        Integer quantity
) {
    public static SupplierPersonalizationResponse from(
            SupplierOrderBatchOrder order,
            SupplierOrderBatchItem item
    ) {
        return new SupplierPersonalizationResponse(
                order.getOriginalOrderId(),
                item.getProductCode(),
                item.getProductName(),
                item.getSize(),
                item.getCustomName(),
                item.getQuantity()
        );
    }
}
