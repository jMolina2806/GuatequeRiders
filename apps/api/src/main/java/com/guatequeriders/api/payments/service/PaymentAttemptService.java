package com.guatequeriders.api.payments.service;

import com.guatequeriders.api.common.exception.ConflictException;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.merch.model.CampaignStatus;
import com.guatequeriders.api.merch.model.MerchCampaign;
import com.guatequeriders.api.merch.repository.MerchCampaignRepository;
import com.guatequeriders.api.orders.model.Order;
import com.guatequeriders.api.orders.model.OrderItem;
import com.guatequeriders.api.orders.model.OrderStatus;
import com.guatequeriders.api.orders.model.PaymentStatus;
import com.guatequeriders.api.orders.repository.OrderRepository;
import com.guatequeriders.api.payments.model.PaymentAttempt;
import com.guatequeriders.api.payments.model.PaymentAttemptStatus;
import com.stripe.model.checkout.Session;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentAttemptService {

    private static final String CURRENCY = "EUR";
    private static final EnumSet<PaymentAttemptStatus> ACTIVE_STATUSES =
            EnumSet.of(PaymentAttemptStatus.CREATED, PaymentAttemptStatus.OPEN);

    private final OrderRepository orderRepository;
    private final MerchCampaignRepository campaignRepository;
    private final com.guatequeriders.api.payments.repository.PaymentAttemptRepository paymentAttemptRepository;

    public PaymentAttemptService(
            OrderRepository orderRepository,
            MerchCampaignRepository campaignRepository,
            com.guatequeriders.api.payments.repository.PaymentAttemptRepository paymentAttemptRepository
    ) {
        this.orderRepository = orderRepository;
        this.campaignRepository = campaignRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
    }

    @Transactional
    public CheckoutPreparation prepareCheckout(Long orderId, com.guatequeriders.api.users.model.User user) {
        MerchCampaign campaign = lockCampaignForOrder(orderId);
        Order order = orderRepository.findWithDetailsByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        assertOwner(order, user);

        if (campaign.getStatus() != CampaignStatus.OPEN) {
            throw new ConflictException("New payments are closed for this Merch campaign");
        }
        if (order.getStatus() != OrderStatus.PENDING
                || order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ConflictException("Only pending unpaid orders can start a payment");
        }

        PaymentAttempt activeAttempt = paymentAttemptRepository
                .findLatestActiveForOrderForUpdate(orderId, ACTIVE_STATUSES)
                .orElse(null);
        if (activeAttempt != null) {
            if (activeAttempt.getStatus() == PaymentAttemptStatus.OPEN
                    && activeAttempt.getExpiresAt() != null
                    && !activeAttempt.getExpiresAt().isAfter(LocalDateTime.now())) {
                activeAttempt.setStatus(PaymentAttemptStatus.EXPIRED);
                paymentAttemptRepository.save(activeAttempt);
                activeAttempt = null;
            } else {
                return toPreparation(activeAttempt);
            }
        }

        long amountMinor = amountMinor(order.getTotal());
        if (amountMinor <= 0) {
            throw new ConflictException("Orders with a zero total cannot be paid");
        }

        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setOrder(order);
        attempt.setStatus(PaymentAttemptStatus.CREATED);
        attempt.setAmountMinor(amountMinor);
        attempt.setCurrency(CURRENCY);
        attempt.setIdempotencyKey(UUID.randomUUID().toString());
        paymentAttemptRepository.saveAndFlush(attempt);
        return toPreparation(attempt);
    }

    @Transactional
    public void markSessionCreated(Long attemptId, Session session) {
        PaymentAttempt attempt = paymentAttemptRepository.findByIdForUpdate(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment attempt", attemptId));
        if (!ACTIVE_STATUSES.contains(attempt.getStatus())) {
            throw new ConflictException("Payment attempt is no longer active");
        }
        if (session.getId() == null || session.getUrl() == null || session.getUrl().isBlank()) {
            throw new ConflictException("Stripe did not return a usable Checkout Session");
        }

        attempt.setStripeCheckoutSessionId(session.getId());
        attempt.setStripePaymentIntentId(session.getPaymentIntent());
        attempt.setCheckoutUrl(session.getUrl());
        attempt.setStatus(PaymentAttemptStatus.OPEN);
        if (session.getExpiresAt() != null) {
            attempt.setExpiresAt(LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(session.getExpiresAt()), ZoneId.systemDefault()));
        }
        paymentAttemptRepository.save(attempt);
    }

    @Transactional
    public void updateCheckoutUrl(Long attemptId, String checkoutUrl) {
        PaymentAttempt attempt = paymentAttemptRepository.findByIdForUpdate(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment attempt", attemptId));
        if (checkoutUrl != null && !checkoutUrl.isBlank()) {
            attempt.setCheckoutUrl(checkoutUrl);
        }
    }

    @Transactional
    public void markExpired(Long attemptId) {
        paymentAttemptRepository.findByIdForUpdate(attemptId).ifPresent(attempt -> {
            if (attempt.getStatus() == PaymentAttemptStatus.OPEN
                    || attempt.getStatus() == PaymentAttemptStatus.CREATED) {
                attempt.setStatus(PaymentAttemptStatus.EXPIRED);
                paymentAttemptRepository.save(attempt);
            }
        });
    }

    @Transactional
    public void markFailed(Long attemptId) {
        paymentAttemptRepository.findByIdForUpdate(attemptId).ifPresent(attempt -> {
            if (attempt.getStatus() != PaymentAttemptStatus.SUCCEEDED) {
                attempt.setStatus(PaymentAttemptStatus.FAILED);
                paymentAttemptRepository.save(attempt);
            }
        });
    }

    @Transactional(readOnly = true)
    public PaymentAttempt findBySessionId(String sessionId) {
        return paymentAttemptRepository.findByStripeCheckoutSessionId(sessionId).orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean isPaymentIntentUsedByAnotherAttempt(String paymentIntentId, Long attemptId) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            return false;
        }
        return paymentAttemptRepository.findByStripePaymentIntentId(paymentIntentId)
                .map(attempt -> !attempt.getId().equals(attemptId))
                .orElse(false);
    }

    public static long amountMinor(BigDecimal amount) {
        if (amount == null) {
            return 0;
        }
        return amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
    }

    private CheckoutPreparation toPreparation(PaymentAttempt attempt) {
        Order order = attempt.getOrder();
        List<CheckoutPreparation.LineItem> lineItems = order.getItems().stream()
                .map(this::lineItem)
                .toList();
        long lineTotal = lineItems.stream()
                .mapToLong(item -> item.unitAmountMinor() * item.quantity())
                .sum();
        if (lineTotal != attempt.getAmountMinor()) {
            throw new ConflictException("Order total does not match its items");
        }
        return new CheckoutPreparation(
                attempt.getId(),
                order.getId(),
                order.getCampaign().getId(),
                attempt.getIdempotencyKey(),
                attempt.getStripeCheckoutSessionId(),
                attempt.getCheckoutUrl(),
                attempt.getAmountMinor(),
                attempt.getCurrency(),
                order.getCustomerEmail(),
                lineItems
        );
    }

    private CheckoutPreparation.LineItem lineItem(OrderItem item) {
        String description = "Talla " + item.getSize() + " x " + item.getQuantity();
        if (item.getCustomName() != null && !item.getCustomName().isBlank()) {
            description += " · Personalización: " + item.getCustomName();
        }
        return new CheckoutPreparation.LineItem(
                item.getProductName(),
                description,
                amountMinor(item.getUnitPrice()),
                item.getQuantity()
        );
    }

    private MerchCampaign lockCampaignForOrder(Long orderId) {
        Long campaignId = orderRepository.findCampaignIdByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return campaignRepository.findByIdForUpdate(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Merch campaign", campaignId));
    }

    private void assertOwner(Order order, com.guatequeriders.api.users.model.User user) {
        if (user == null || !order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to this order");
        }
    }
}
