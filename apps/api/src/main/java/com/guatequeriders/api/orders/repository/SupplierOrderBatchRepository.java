package com.guatequeriders.api.orders.repository;

import com.guatequeriders.api.orders.model.SupplierOrderBatch;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SupplierOrderBatchRepository extends JpaRepository<SupplierOrderBatch, Long> {

    @Query("select count(batch) > 0 from SupplierOrderBatch batch where batch.campaign.id = :campaignId")
    boolean existsByCampaignId(@Param("campaignId") Long campaignId);

    @EntityGraph(attributePaths = {"campaign", "orders", "orders.items"})
    @Query("select batch from SupplierOrderBatch batch where batch.campaign.id = :campaignId")
    Optional<SupplierOrderBatch> findByCampaignId(@Param("campaignId") Long campaignId);
}
