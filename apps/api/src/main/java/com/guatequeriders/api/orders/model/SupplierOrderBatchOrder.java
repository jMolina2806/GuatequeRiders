package com.guatequeriders.api.orders.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supplier_order_batch_orders")
public class SupplierOrderBatchOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false, updatable = false)
    private SupplierOrderBatch batch;

    @Column(name = "original_order_id", nullable = false, updatable = false)
    private Long originalOrderId;

    @Column(name = "customer_name", nullable = false, length = 255, updatable = false)
    private String customerName;

    @Column(name = "order_created_at", nullable = false, updatable = false)
    private LocalDateTime orderCreatedAt;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2, updatable = false)
    private BigDecimal total;

    @OneToMany(mappedBy = "batchOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<SupplierOrderBatchItem> items = new ArrayList<>();

    public void addItem(SupplierOrderBatchItem item) {
        items.add(item);
        item.setBatchOrder(this);
    }

    public Long getId() { return id; }

    public SupplierOrderBatch getBatch() { return batch; }
    public void setBatch(SupplierOrderBatch batch) { this.batch = batch; }

    public Long getOriginalOrderId() { return originalOrderId; }
    public void setOriginalOrderId(Long originalOrderId) { this.originalOrderId = originalOrderId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDateTime getOrderCreatedAt() { return orderCreatedAt; }
    public void setOrderCreatedAt(LocalDateTime orderCreatedAt) { this.orderCreatedAt = orderCreatedAt; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<SupplierOrderBatchItem> getItems() { return items; }
}
