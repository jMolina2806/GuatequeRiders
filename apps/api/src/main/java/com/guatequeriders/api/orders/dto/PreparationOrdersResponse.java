package com.guatequeriders.api.orders.dto;

import com.guatequeriders.api.merch.model.ProductCode;
import com.guatequeriders.api.merch.model.ProductSize;
import com.guatequeriders.api.orders.model.Order;
import com.guatequeriders.api.orders.model.OrderItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record PreparationOrdersResponse(
        List<OrderResponse> orders,
        List<PreparationSummaryResponse> summary,
        List<PreparationProductSummaryResponse> productSummary,
        int totalUnits
) {
    public static PreparationOrdersResponse from(List<Order> paidOrders) {
        Map<String, Integer> quantities = new LinkedHashMap<>();
        Map<ProductCode, Integer> productQuantities = new LinkedHashMap<>();
        for (ProductCode product : ProductCode.values()) {
            productQuantities.put(product, 0);
            for (ProductSize size : ProductSize.values()) {
                quantities.put(key(product, size), 0);
            }
        }

        int totalUnits = 0;
        for (Order order : paidOrders) {
            for (OrderItem item : order.getItems()) {
                ProductCode product = ProductCode.valueOf(item.getProductCode());
                ProductSize size = ProductSize.valueOf(item.getSize());
                quantities.merge(key(product, size), item.getQuantity(), Integer::sum);
                productQuantities.merge(product, item.getQuantity(), Integer::sum);
                totalUnits += item.getQuantity();
            }
        }

        List<PreparationSummaryResponse> summary = new ArrayList<>();
        for (ProductCode product : ProductCode.values()) {
            for (ProductSize size : ProductSize.values()) {
                summary.add(new PreparationSummaryResponse(
                        product.name(),
                        product.getDisplayName(),
                        size,
                        quantities.get(key(product, size))
                ));
            }
        }

        List<PreparationProductSummaryResponse> productSummary = productQuantities.entrySet().stream()
                .map(entry -> new PreparationProductSummaryResponse(
                        entry.getKey().name(),
                        entry.getKey().getDisplayName(),
                        entry.getValue()
                ))
                .toList();

        return new PreparationOrdersResponse(
                paidOrders.stream().map(OrderResponse::from).toList(),
                summary,
                productSummary,
                totalUnits
        );
    }

    private static String key(ProductCode product, ProductSize size) {
        return product.name() + ":" + size.name();
    }
}
