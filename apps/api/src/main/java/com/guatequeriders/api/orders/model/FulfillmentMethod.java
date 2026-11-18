package com.guatequeriders.api.orders.model;

public enum FulfillmentMethod {
    PICKUP,
    SHIPPING;

    public static FulfillmentMethod fromValue(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid fulfillmentMethod: " + value);
        }
    }
}
