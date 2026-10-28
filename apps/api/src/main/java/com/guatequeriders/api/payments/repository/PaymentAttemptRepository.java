package com.guatequeriders.api.payments.repository;

import com.guatequeriders.api.payments.model.PaymentAttempt;
import com.guatequeriders.api.payments.model.PaymentAttemptStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select attempt from PaymentAttempt attempt "
            + "join fetch attempt.order order "
            + "join fetch order.campaign campaign "
            + "join fetch order.user user "
            + "where attempt.order.id = :orderId and attempt.status in :statuses "
            + "order by attempt.id desc")
    Optional<PaymentAttempt> findLatestActiveForOrderForUpdate(
            @Param("orderId") Long orderId,
            @Param("statuses") Collection<PaymentAttemptStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select attempt from PaymentAttempt attempt "
            + "join fetch attempt.order order "
            + "join fetch order.campaign campaign "
            + "join fetch order.user user "
            + "where attempt.id = :id")
    Optional<PaymentAttempt> findByIdForUpdate(@Param("id") Long id);

    Optional<PaymentAttempt> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    Optional<PaymentAttempt> findByStripePaymentIntentId(String stripePaymentIntentId);

    boolean existsByOrderIdAndStatusIn(Long orderId, Collection<PaymentAttemptStatus> statuses);
}
