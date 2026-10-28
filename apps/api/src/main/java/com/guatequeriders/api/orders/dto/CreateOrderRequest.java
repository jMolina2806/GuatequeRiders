package com.guatequeriders.api.orders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateOrderRequest(
        @NotEmpty(message = "At least one item is required")
        @Valid
        List<OrderItemRequest> items,

        @NotBlank(message = "Customer name is required")
        @Size(max = 255, message = "Customer name is too long")
        String customerName,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Customer email must be valid")
        @Size(max = 255, message = "Customer email is too long")
        String customerEmail,

        @NotBlank(message = "Customer phone is required")
        @Size(max = 50, message = "Customer phone is too long")
        String customerPhone,

        @Size(max = 1000, message = "Notes are too long")
        String notes,

        @Size(max = 20, message = "Fulfillment method is too long")
        String fulfillmentMethod,

        @Size(max = 1000, message = "Address is too long")
        String address,

        @Size(max = 255, message = "City is too long")
        String city,

        @Size(max = 30, message = "Postal code is too long")
        String postalCode,

        @Size(max = 120, message = "Province is too long")
        String province
) {
}
