package com.guatequeriders.api.payments;

import com.guatequeriders.api.merch.model.CampaignStatus;
import com.guatequeriders.api.merch.model.MerchCampaign;
import com.guatequeriders.api.orders.model.Order;
import com.guatequeriders.api.orders.model.PaymentStatus;
import com.guatequeriders.api.orders.service.OrdersService;
import com.guatequeriders.api.payments.config.StripeMode;
import com.guatequeriders.api.payments.config.StripeProperties;
import com.guatequeriders.api.payments.model.PaymentApplicationResult;
import com.guatequeriders.api.payments.model.PaymentAttempt;
import com.guatequeriders.api.payments.model.PaymentAttemptStatus;
import com.guatequeriders.api.payments.model.StripeEvent;
import com.guatequeriders.api.payments.model.StripeEventStatus;
import com.guatequeriders.api.payments.repository.StripeEventRepository;
import com.guatequeriders.api.payments.service.PaymentAttemptService;
import com.guatequeriders.api.payments.service.StripeWebhookService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class StripeWebhookServiceTest {

    private StripeEventRepository stripeEventRepository;
    private PaymentAttemptService paymentAttemptService;
    private OrdersService ordersService;
    private StripeWebhookService service;
    private PaymentAttempt attempt;

    @BeforeEach
    void setUp() {
        stripeEventRepository = mock(StripeEventRepository.class);
        paymentAttemptService = mock(PaymentAttemptService.class);
        ordersService = mock(OrdersService.class);

        StripeProperties properties = new StripeProperties();
        properties.setEnabled(true);
        properties.setMode(StripeMode.TEST);

        service = new StripeWebhookService(
                stripeEventRepository,
                paymentAttemptService,
                ordersService,
                properties
        );

        Order order = new Order();
        order.setId(11L);
        order.setPaymentStatus(PaymentStatus.PENDING);
        MerchCampaign campaign = new MerchCampaign();
        campaign.setId(22L);
        campaign.setStatus(CampaignStatus.OPEN);
        order.setCampaign(campaign);

        attempt = new PaymentAttempt();
        attempt.setId(33L);
        attempt.setOrder(order);
        attempt.setStripeCheckoutSessionId("cs_test_123");
        attempt.setStripePaymentIntentId("pi_test_123");
        attempt.setAmountMinor(2000L);
        attempt.setCurrency("EUR");
        attempt.setStatus(PaymentAttemptStatus.OPEN);

        when(stripeEventRepository.findByStripeEventId(any())).thenReturn(Optional.empty());
        when(paymentAttemptService.findBySessionId("cs_test_123")).thenReturn(attempt);
        when(paymentAttemptService.isPaymentIntentUsedByAnotherAttempt("pi_test_123", 33L))
                .thenReturn(false);
    }

    @Test
    void completedWithoutPaidStatusLeavesOrderPending() {
        Event event = event("evt_unpaid", "checkout.session.completed", false);
        Session session = session("unpaid");

        service.process(event, session);

        verify(ordersService, never()).applyVerifiedPaymentWebhook(anyLong(), anyLong(), any(), any(), anyLong(), any());
        verify(paymentAttemptService, never()).markFailed(33L);
        assertThat(lastSavedEvent().getStatus()).isEqualTo(StripeEventStatus.PROCESSED);
        assertThat(attempt.getOrder().getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void asynchronousSuccessMarksOrderPaidAfterValidation() {
        Event event = event("evt_async_success", "checkout.session.async_payment_succeeded", false);
        Session session = session("paid");
        when(ordersService.applyVerifiedPaymentWebhook(11L, 33L, "cs_test_123", "pi_test_123", 2000L, "EUR"))
                .thenReturn(PaymentApplicationResult.PAID);

        service.process(event, session);

        verify(ordersService).applyVerifiedPaymentWebhook(11L, 33L, "cs_test_123", "pi_test_123", 2000L, "EUR");
        assertThat(lastSavedEvent().getStatus()).isEqualTo(StripeEventStatus.PROCESSED);
    }

    @Test
    void asynchronousFailureMarksAttemptFailedWithoutApplyingPayment() {
        Event event = event("evt_async_failed", "checkout.session.async_payment_failed", false);
        Session session = session("unpaid");

        service.process(event, session);

        verify(paymentAttemptService).markFailed(33L);
        verify(ordersService, never()).applyVerifiedPaymentWebhook(anyLong(), anyLong(), any(), any(), anyLong(), any());
        assertThat(lastSavedEvent().getStatus()).isEqualTo(StripeEventStatus.PROCESSED);
    }

    @Test
    void amountMismatchNeverMarksOrderPaid() {
        Event event = event("evt_amount_mismatch", "checkout.session.completed", false);
        Session session = session("paid");
        session.setAmountTotal(1200L);

        service.process(event, session);

        verify(ordersService, never()).applyVerifiedPaymentWebhook(anyLong(), anyLong(), any(), any(), anyLong(), any());
        assertThat(lastSavedEvent().getStatus()).isEqualTo(StripeEventStatus.FAILED);
        assertThat(lastSavedEvent().getFailureReason()).isEqualTo("AMOUNT_MISMATCH");
    }

    @Test
    void wrongLivemodeDoesNotTouchDatabase() {
        Event event = event("evt_live", "checkout.session.completed", true);
        Session session = session("paid");

        service.process(event, session);

        verify(stripeEventRepository, never()).save(any());
        verify(paymentAttemptService, never()).findBySessionId(any());
        verify(ordersService, never()).applyVerifiedPaymentWebhook(anyLong(), anyLong(), any(), any(), anyLong(), any());
    }

    @Test
    void duplicateEventHasNoSecondEffect() {
        Event event = event("evt_duplicate", "checkout.session.completed", false);
        Session session = session("paid");
        StripeEvent existing = new StripeEvent();
        existing.setStatus(StripeEventStatus.PROCESSED);
        when(stripeEventRepository.findByStripeEventId("evt_duplicate")).thenReturn(Optional.of(existing));

        service.process(event, session);

        verify(paymentAttemptService, never()).findBySessionId(any());
        verify(ordersService, never()).applyVerifiedPaymentWebhook(anyLong(), anyLong(), any(), any(), anyLong(), any());
    }

    private Event event(String id, String type, boolean livemode) {
        Event event = new Event();
        event.setId(id);
        event.setType(type);
        event.setLivemode(livemode);
        return event;
    }

    private Session session(String paymentStatus) {
        Session session = new Session();
        session.setId("cs_test_123");
        session.setLivemode(false);
        session.setMode("payment");
        session.setPaymentStatus(paymentStatus);
        session.setAmountTotal(2000L);
        session.setCurrency("eur");
        session.setPaymentIntent("pi_test_123");
        session.setClientReferenceId("11");
        session.setMetadata(Map.of(
                "order_id", "11",
                "campaign_id", "22",
                "payment_attempt_id", "33"
        ));
        return session;
    }

    private StripeEvent lastSavedEvent() {
        ArgumentCaptor<StripeEvent> captor = ArgumentCaptor.forClass(StripeEvent.class);
        verify(stripeEventRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        return captor.getAllValues().get(captor.getAllValues().size() - 1);
    }
}
