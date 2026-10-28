package com.guatequeriders.api.orders.repository;

import com.guatequeriders.api.orders.model.Order;
import com.guatequeriders.api.orders.model.PaymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "user", "campaign"})
    @Query("select order from Order order where order.id = :id")
    Optional<Order> findWithDetailsById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"items", "user", "campaign"})
    @Query("select order from Order order where order.id = :id")
    Optional<Order> findWithDetailsByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items", "user", "campaign"})
    List<Order> findAllByUserIdOrderByCreatedAtDescIdDesc(Long userId);

    @EntityGraph(attributePaths = {"items", "user", "campaign"})
    List<Order> findAllByCampaignIdOrderByCreatedAtDescIdDesc(Long campaignId);

    @EntityGraph(attributePaths = {"items", "user", "campaign"})
    List<Order> findAllByCampaignIdAndPaymentStatusOrderByCreatedAtDescIdDesc(
            Long campaignId,
            PaymentStatus paymentStatus
    );

    @Query("select order.campaign.id from Order order where order.id = :id")
    Optional<Long> findCampaignIdByOrderId(@Param("id") Long id);

}
