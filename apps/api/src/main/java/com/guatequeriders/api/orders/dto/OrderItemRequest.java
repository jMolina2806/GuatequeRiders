package com.guatequeriders.api.orders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderItemRequest(
        @NotBlank(message = "Product code is required")
        String productCode,

        @NotBlank(message = "Size is required")
        String size,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than 0")
        @Max(value = 10, message = "Quantity must not exceed 10 per line")
        Integer quantity,

        String customName
) {
}
