package com.guatequeriders.api.orders.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_order_batch_items")
public class SupplierOrderBatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_order_id", nullable = false, updatable = false)
    private SupplierOrderBatchOrder batchOrder;

    @Column(name = "product_code", nullable = false, length = 20, updatable = false)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 255, updatable = false)
    private String productName;

    @Column(name = "size", nullable = false, length = 10, updatable = false)
    private String size;

    @Column(nullable = false, updatable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2, updatable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2, updatable = false)
    private BigDecimal subtotal;

    @Column(name = "custom_name", length = 20, updatable = false)
    private String customName;

    public Long getId() { return id; }

    public SupplierOrderBatchOrder getBatchOrder() { return batchOrder; }
    public void setBatchOrder(SupplierOrderBatchOrder batchOrder) { this.batchOrder = batchOrder; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }
}
