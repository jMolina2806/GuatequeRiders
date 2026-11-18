package com.guatequeriders.api.merch.model;

import java.math.BigDecimal;

public enum ProductCode {
    TSHIRT("Camiseta Guateque Riders", "20.00"),
    CROP_TOP("Crop Top Guateque Riders", "12.00"),
    HOODIE("Sudadera Guateque Riders", "30.00");

    private final String displayName;
    private final BigDecimal price;

    ProductCode(String displayName, String price) {
        this.displayName = displayName;
        this.price = new BigDecimal(price);
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public static ProductCode fromCode(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Invalid productCode: " + code);
        }
    }
}
