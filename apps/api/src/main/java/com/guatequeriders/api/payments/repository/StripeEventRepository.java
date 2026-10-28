package com.guatequeriders.api.payments.repository;

import com.guatequeriders.api.payments.model.StripeEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StripeEventRepository extends JpaRepository<StripeEvent, Long> {

    Optional<StripeEvent> findByStripeEventId(String stripeEventId);
}
