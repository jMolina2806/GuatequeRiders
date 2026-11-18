package com.guatequeriders.api.payments;

import com.guatequeriders.api.common.exception.ConflictException;
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
import com.guatequeriders.api.payments.repository.PaymentAttemptRepository;
import com.guatequeriders.api.payments.service.CheckoutPreparation;
import com.guatequeriders.api.payments.service.PaymentAttemptService;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class PaymentAttemptServiceTest {

    private OrderRepository orderRepository;
    private MerchCampaignRepository campaignRepository;
    private PaymentAttemptRepository paymentAttemptRepository;
    private PaymentAttemptService service;
    private Order order;
    private MerchCampaign campaign;
    private User owner;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        campaignRepository = mock(MerchCampaignRepository.class);
        paymentAttemptRepository = mock(PaymentAttemptRepository.class);
        service = new PaymentAttemptService(orderRepository, campaignRepository, paymentAttemptRepository);

        owner = new User(7L, "demo", "demo@example.com", "password", Role.USER, null);
        campaign = new MerchCampaign();
        campaign.setId(22L);
        campaign.setStatus(CampaignStatus.OPEN);

        order = new Order();
        order.setId(11L);
        order.setUser(owner);
        order.setCampaign(campaign);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setTotal(new BigDecimal("20.00"));
        OrderItem item = new OrderItem();
        item.setProductName("Camiseta Guateque Riders");
        item.setSize("M");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("20.00"));
        order.addItem(item);

        when(orderRepository.findCampaignIdByOrderId(11L)).thenReturn(Optional.of(22L));
        when(campaignRepository.findByIdForUpdate(22L)).thenReturn(Optional.of(campaign));
        when(orderRepository.findWithDetailsByIdForUpdate(11L)).thenReturn(Optional.of(order));
        when(paymentAttemptRepository.findLatestActiveForOrderForUpdate(anyLong(), any()))
                .thenReturn(Optional.empty());
    }

    @Test
    void reusesTheOnlyActiveAttemptForAnOrder() {
        PaymentAttempt active = new PaymentAttempt();
        active.setId(33L);
        active.setOrder(order);
        active.setStatus(PaymentAttemptStatus.OPEN);
        active.setAmountMinor(2000L);
        active.setCurrency("EUR");
        active.setIdempotencyKey("attempt-key");
        active.setStripeCheckoutSessionId("cs_test_123");
        active.setCheckoutUrl("https://checkout.stripe.test/cs_test_123");
        when(paymentAttemptRepository.findLatestActiveForOrderForUpdate(11L,
                EnumSet.of(PaymentAttemptStatus.CREATED, PaymentAttemptStatus.OPEN)))
                .thenReturn(Optional.of(active));

        CheckoutPreparation preparation = service.prepareCheckout(11L, owner);

        assertThat(preparation.paymentAttemptId()).isEqualTo(33L);
        assertThat(preparation.sessionId()).isEqualTo("cs_test_123");
        assertThat(preparation.checkoutUrl()).isEqualTo("https://checkout.stripe.test/cs_test_123");
        verify(paymentAttemptRepository, never()).saveAndFlush(any());
    }

    @Test
    void closedCampaignRejectsNewCheckoutAttempt() {
        campaign.setStatus(CampaignStatus.CLOSED);

        assertThatThrownBy(() -> service.prepareCheckout(11L, owner))
                .isInstanceOf(ConflictException.class)
                .hasMessage("New payments are closed for this Merch campaign");
        verify(paymentAttemptRepository, never()).saveAndFlush(any());
    }

    @Test
    void anotherUserCannotPrepareCheckoutForTheOrder() {
        User otherUser = new User(8L, "other", "other@example.com", "password", Role.USER, null);

        assertThatThrownBy(() -> service.prepareCheckout(11L, otherUser))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
        verify(paymentAttemptRepository, never()).saveAndFlush(any());
    }
}
