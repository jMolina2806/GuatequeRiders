package com.guatequeriders.api.orders.dto;

import com.guatequeriders.api.merch.model.ProductCode;
import com.guatequeriders.api.merch.model.ProductSize;
import com.guatequeriders.api.orders.model.SupplierOrderBatch;
import com.guatequeriders.api.orders.model.SupplierOrderBatchItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record SupplierOrderBatchResponse(
        Long id,
        Long campaignId,
        Long campaignNumber,
        LocalDateTime closedAt,
        Integer totalUnits,
        BigDecimal totalAmount,
        List<PreparationSummaryResponse> summary,
        List<PreparationProductSummaryResponse> productSummary,
        List<SupplierPersonalizationResponse> personalizations,
        List<SupplierOrderBatchOrderResponse> orders
) {
    public static SupplierOrderBatchResponse from(SupplierOrderBatch batch) {
        Map<String, Integer> quantities = new LinkedHashMap<>();
        Map<ProductCode, Integer> productQuantities = new LinkedHashMap<>();
        for (ProductCode product : ProductCode.values()) {
            productQuantities.put(product, 0);
            for (ProductSize size : ProductSize.values()) {
                quantities.put(key(product, size), 0);
            }
        }

        for (var batchOrder : batch.getOrders()) {
            for (SupplierOrderBatchItem item : batchOrder.getItems()) {
                ProductCode product = ProductCode.valueOf(item.getProductCode());
                ProductSize size = ProductSize.valueOf(item.getSize());
                quantities.merge(key(product, size), item.getQuantity(), Integer::sum);
                productQuantities.merge(product, item.getQuantity(), Integer::sum);
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

        List<SupplierPersonalizationResponse> personalizations = batch.getOrders().stream()
                .flatMap(order -> order.getItems().stream()
                        .filter(item -> item.getCustomName() != null)
                        .map(item -> SupplierPersonalizationResponse.from(order, item)))
                .toList();

        return new SupplierOrderBatchResponse(
                batch.getId(),
                batch.getCampaignId(),
                batch.getCampaignId(),
                batch.getClosedAt(),
                batch.getTotalUnits(),
                batch.getTotalAmount(),
                summary,
                productSummary,
                personalizations,
                batch.getOrders().stream().map(SupplierOrderBatchOrderResponse::from).toList()
        );
    }

    private static String key(ProductCode product, ProductSize size) {
        return product.name() + ":" + size.name();
    }
}
