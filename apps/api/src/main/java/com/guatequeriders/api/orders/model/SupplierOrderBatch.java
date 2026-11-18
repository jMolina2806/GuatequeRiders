package com.guatequeriders.api.orders.model;

import com.guatequeriders.api.merch.model.MerchCampaign;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "supplier_order_batches")
public class SupplierOrderBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false, unique = true, updatable = false)
    private MerchCampaign campaign;

    @Column(name = "closed_at", nullable = false, updatable = false)
    private LocalDateTime closedAt;

    @Column(name = "total_units", nullable = false, updatable = false)
    private Integer totalUnits;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2, updatable = false)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private Set<SupplierOrderBatchOrder> orders = new LinkedHashSet<>();

    @PrePersist
    void onCreate() {
        if (closedAt == null) {
            closedAt = LocalDateTime.now();
        }
        if (totalUnits == null) {
            totalUnits = 0;
        }
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO.setScale(2);
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void addOrder(SupplierOrderBatchOrder order) {
        orders.add(order);
        order.setBatch(this);
    }

    public Long getId() { return id; }
    public Long getCampaignId() { return campaign.getId(); }

    public MerchCampaign getCampaign() { return campaign; }
    public void setCampaign(MerchCampaign campaign) { this.campaign = campaign; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public Integer getTotalUnits() { return totalUnits; }
    public void setTotalUnits(Integer totalUnits) { this.totalUnits = totalUnits; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Set<SupplierOrderBatchOrder> getOrders() { return orders; }
}
