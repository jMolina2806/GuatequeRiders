package com.guatequeriders.api.merch.model;

public enum ProductSize {
    S,
    M,
    L,
    XL;

    public static ProductSize fromValue(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Invalid size: " + value);
        }
    }
}
