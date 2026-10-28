package com.guatequeriders.api.orders.service;

import com.guatequeriders.api.common.exception.ConflictException;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.merch.model.CampaignStatus;
import com.guatequeriders.api.merch.model.MerchCampaign;
import com.guatequeriders.api.merch.repository.MerchCampaignRepository;
import com.guatequeriders.api.merch.model.ProductCode;
import com.guatequeriders.api.merch.model.ProductSize;
import com.guatequeriders.api.orders.dto.CreateOrderRequest;
import com.guatequeriders.api.orders.dto.OrderItemRequest;
import com.guatequeriders.api.orders.model.Order;
import com.guatequeriders.api.orders.model.OrderItem;
import com.guatequeriders.api.orders.model.OrderStatus;
import com.guatequeriders.api.orders.model.PaymentStatus;
import com.guatequeriders.api.orders.model.FulfillmentMethod;
import com.guatequeriders.api.orders.repository.OrderRepository;
import com.guatequeriders.api.payments.model.PaymentApplicationResult;
import com.guatequeriders.api.payments.model.PaymentAttempt;
import com.guatequeriders.api.payments.model.PaymentAttemptStatus;
import com.guatequeriders.api.payments.repository.PaymentAttemptRepository;
import com.guatequeriders.api.payments.service.StripeCheckoutService;
import com.guatequeriders.api.orders.validation.CustomNameNormalizer;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Service
public class OrdersService {

    private static final Logger log = LoggerFactory.getLogger(OrdersService.class);

    private final OrderRepository orderRepository;
    private final MerchCampaignRepository campaignRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final ObjectProvider<StripeCheckoutService> stripeCheckoutServiceProvider;

    public OrdersService(OrderRepository orderRepository,
                         MerchCampaignRepository campaignRepository,
                         PaymentAttemptRepository paymentAttemptRepository,
                         ObjectProvider<StripeCheckoutService> stripeCheckoutServiceProvider) {
        this.orderRepository = orderRepository;
        this.campaignRepository = campaignRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.stripeCheckoutServiceProvider = stripeCheckoutServiceProvider;
    }

    @Transactional
    public Order create(CreateOrderRequest request, User user) {
        MerchCampaign campaign = campaignRepository.findByStatusForUpdate(CampaignStatus.OPEN)
                .orElseThrow(() -> new ConflictException("Merch store is currently closed"));

        Order order = new Order();
        order.setUser(user);
        order.setCampaign(campaign);
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setCustomerPhone(request.customerPhone());
        order.setNotes(request.notes());
        FulfillmentMethod fulfillmentMethod = FulfillmentMethod.fromValue(request.fulfillmentMethod());
        order.setFulfillmentMethod(fulfillmentMethod == null ? null : fulfillmentMethod.name());
        order.setAddress(request.address());
        order.setCity(request.city());
        order.setPostalCode(request.postalCode());
        order.setProvince(request.province());

        BigDecimal total = BigDecimal.ZERO.setScale(2);
        Set<String> lineKeys = new HashSet<>();
        for (OrderItemRequest requestedItem : request.items()) {
            ProductCode product = ProductCode.fromCode(requestedItem.productCode());
            ProductSize size = ProductSize.fromValue(requestedItem.size());
            int quantity = requestedItem.quantity();
            if (quantity < 1 || quantity > 10) {
                throw new IllegalArgumentException("Quantity must be between 1 and 10 per line");
            }
            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2);
            String customName = CustomNameNormalizer.normalize(requestedItem.customName());
            String lineKey = product.name() + ":" + size.name() + ":" + CustomNameNormalizer.key(customName);
            if (!lineKeys.add(lineKey)) {
                throw new IllegalArgumentException("Duplicate product, size and custom name lines are not allowed");
            }
            OrderItem item = new OrderItem();
            item.setProductCode(product.name());
            item.setProductName(product.getDisplayName());
            item.setSize(size.name());
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setSubtotal(subtotal);
            item.setCustomName(customName);
            order.addItem(item);
            total = total.add(subtotal);
        }
        order.setTotal(total);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getForUser(Long id, User user) {
        Order order = orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        assertOwnerOrAdmin(order, user);
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> findForUser(User user) {
        return orderRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(user.getId());
    }

    @Transactional
    public void assertPaymentCanStart(Long orderId) {
        MerchCampaign campaign = lockCampaignForOrder(orderId);
        if (campaign.getStatus() != CampaignStatus.OPEN) {
            throw new ConflictException("New payments are closed for this Merch campaign");
        }
    }

    @Transactional
    public Order applyPaymentWebhook(Long orderId) {
        MerchCampaign campaign = lockCampaignForOrder(orderId);
        Order order = orderRepository.findWithDetailsByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return order;
        }
        if (order.getStatus() == OrderStatus.CANCELLED
                || order.getPaymentStatus() == PaymentStatus.CANCELLED) {
            throw new ConflictException("Cancelled orders cannot be marked as paid");
        }
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaidAt(java.time.LocalDateTime.now());
        order.setStatus(OrderStatus.CONFIRMED);
        order.setLatePayment(campaign.getStatus() != CampaignStatus.OPEN);
        return order;
    }

    @Transactional
    public PaymentApplicationResult applyVerifiedPaymentWebhook(
            Long orderId,
            Long paymentAttemptId,
            String sessionId,
            String paymentIntentId,
            long amountMinor,
            String currency
    ) {
        MerchCampaign campaign = lockCampaignForOrder(orderId);
        Order order = orderRepository.findWithDetailsByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        PaymentAttempt attempt = paymentAttemptRepository.findByIdForUpdate(paymentAttemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment attempt", paymentAttemptId));

        if (!order.getId().equals(attempt.getOrder().getId())
                || !orderId.equals(attempt.getOrder().getId())
                || !sessionId.equals(attempt.getStripeCheckoutSessionId())
                || attempt.getAmountMinor() != amountMinor
                || !attempt.getCurrency().equalsIgnoreCase(currency)) {
            return PaymentApplicationResult.INVALID_ATTEMPT;
        }
        if (paymentIntentId != null && !paymentIntentId.isBlank()
                && attempt.getStripePaymentIntentId() != null
                && !paymentIntentId.equals(attempt.getStripePaymentIntentId())) {
            return PaymentApplicationResult.INVALID_ATTEMPT;
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            if (attempt.getStatus() == PaymentAttemptStatus.CREATED
                    || attempt.getStatus() == PaymentAttemptStatus.OPEN) {
                if (paymentIntentId != null && !paymentIntentId.isBlank()) {
                    attempt.setStripePaymentIntentId(paymentIntentId);
                }
                attempt.setStatus(PaymentAttemptStatus.SUCCEEDED);
                attempt.setCompletedAt(java.time.LocalDateTime.now());
                paymentAttemptRepository.save(attempt);
            }
            return PaymentApplicationResult.ALREADY_PAID;
        }
        if (order.getStatus() == OrderStatus.CANCELLED
                || order.getPaymentStatus() == PaymentStatus.CANCELLED) {
            return PaymentApplicationResult.ORDER_CANCELLED;
        }
        if (attempt.getStatus() != PaymentAttemptStatus.CREATED
                && attempt.getStatus() != PaymentAttemptStatus.OPEN) {
            return PaymentApplicationResult.INVALID_ATTEMPT;
        }

        if (paymentIntentId != null && !paymentIntentId.isBlank()) {
            attempt.setStripePaymentIntentId(paymentIntentId);
        }
        attempt.setStatus(PaymentAttemptStatus.SUCCEEDED);
        attempt.setCompletedAt(java.time.LocalDateTime.now());
        paymentAttemptRepository.save(attempt);

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaidAt(java.time.LocalDateTime.now());
        order.setStatus(OrderStatus.CONFIRMED);
        order.setLatePayment(campaign.getStatus() != CampaignStatus.OPEN);
        return PaymentApplicationResult.PAID;
    }

    @Transactional
    public Order simulateDevelopmentPayment(Long orderId, User user) {
        Order order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        assertOwner(order, user);
        if (order.getCampaign().getStatus() != CampaignStatus.OPEN) {
            throw new ConflictException("Development payment simulation requires an open Merch campaign");
        }
        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ConflictException("Only pending orders can use the development payment simulator");
        }
        log.warn("Development payment simulation for order {} requested by user {}", orderId, user.getUsername());
        return applyPaymentWebhook(orderId);
    }

    @Transactional
    public Order cancel(Long id, User user) {
        Order order = orderRepository.findWithDetailsByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        assertOwner(order, user);
        if (order.getStatus() != OrderStatus.PENDING
                || order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ConflictException("Only pending unpaid orders can be cancelled");
        }
        PaymentAttempt activeAttempt = paymentAttemptRepository
                .findLatestActiveForOrderForUpdate(id,
                        java.util.EnumSet.of(PaymentAttemptStatus.OPEN))
                .orElse(null);
        if (activeAttempt != null) {
            if (activeAttempt.getExpiresAt() != null
                    && !activeAttempt.getExpiresAt().isAfter(java.time.LocalDateTime.now())) {
                activeAttempt.setStatus(PaymentAttemptStatus.EXPIRED);
                paymentAttemptRepository.save(activeAttempt);
            } else {
                StripeCheckoutService stripeCheckoutService = stripeCheckoutServiceProvider.getIfAvailable();
                if (stripeCheckoutService == null) {
                    throw new ConflictException("No se puede cancelar el pedido mientras la sesión de pago está activa");
                }
                stripeCheckoutService.expireCheckoutSession(activeAttempt.getStripeCheckoutSessionId());
                activeAttempt.setStatus(PaymentAttemptStatus.EXPIRED);
                paymentAttemptRepository.save(activeAttempt);
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.CANCELLED);
        return order;
    }

    private void assertOwnerOrAdmin(Order order, User user) {
        if (user.getRole() != Role.ADMIN && !order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to this order");
        }
    }

    private void assertOwner(Order order, User user) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to this order");
        }
    }

    private MerchCampaign lockCampaignForOrder(Long orderId) {
        Long campaignId = orderRepository.findCampaignIdByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return campaignRepository.findByIdForUpdate(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Merch campaign", campaignId));
    }
}
