package com.guatequeriders.api.payments;

import com.guatequeriders.api.payments.config.StripeMode;
import com.guatequeriders.api.payments.config.StripeProperties;
import com.guatequeriders.api.payments.dto.CheckoutSessionResponse;
import com.guatequeriders.api.payments.service.CheckoutPreparation;
import com.guatequeriders.api.payments.service.PaymentAttemptService;
import com.guatequeriders.api.payments.service.StripeCheckoutService;
import com.stripe.StripeClient;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StripeCheckoutServiceTest {

    private StripeClient stripeClient;
    private PaymentAttemptService paymentAttemptService;
    private StripeCheckoutService service;
    private Session stripeSession;

    @BeforeEach
    void setUp() throws Exception {
        stripeClient = mock(StripeClient.class, RETURNS_DEEP_STUBS);
        paymentAttemptService = mock(PaymentAttemptService.class);

        StripeProperties properties = new StripeProperties();
        properties.setEnabled(true);
        properties.setMode(StripeMode.TEST);
        properties.setSecretKey("sk_test_key");
        properties.setWebhookSecret("whsec_test");
        properties.setPublicUrl("http://localhost:5173");

        service = new StripeCheckoutService(stripeClient, properties, paymentAttemptService);

        stripeSession = new Session();
        stripeSession.setId("cs_test_123");
        stripeSession.setUrl("https://checkout.stripe.test/cs_test_123");
        stripeSession.setLivemode(false);
        stripeSession.setStatus("open");
        stripeSession.setMode("payment");
        stripeSession.setAmountTotal(2000L);
        stripeSession.setCurrency("eur");
        stripeSession.setPaymentIntent("pi_test_123");
        stripeSession.setClientReferenceId("11");
        stripeSession.setMetadata(Map.of(
                "order_id", "11",
                "campaign_id", "22",
                "payment_attempt_id", "33"
        ));
        when(stripeClient.v1().checkout().sessions().create(any(SessionCreateParams.class), any(RequestOptions.class)))
                .thenReturn(stripeSession);
        when(stripeClient.v1().checkout().sessions().retrieve("cs_test_123"))
                .thenReturn(stripeSession);
        when(paymentAttemptService.isPaymentIntentUsedByAnotherAttempt("pi_test_123", 33L))
                .thenReturn(false);
    }

    @Test
    void createsCardOnlySessionWithServerUrlsAndIdempotencyKey() throws Exception {
        CheckoutPreparation preparation = preparation(null, null);
        when(paymentAttemptService.prepareCheckout(11L, null)).thenReturn(preparation);

        CheckoutSessionResponse response = service.createCheckoutSession(11L, null);

        assertThat(response.sessionId()).isEqualTo("cs_test_123");
        assertThat(response.checkoutUrl()).isEqualTo("https://checkout.stripe.test/cs_test_123");

        ArgumentCaptor<SessionCreateParams> paramsCaptor = ArgumentCaptor.forClass(SessionCreateParams.class);
        ArgumentCaptor<RequestOptions> optionsCaptor = ArgumentCaptor.forClass(RequestOptions.class);
        verify(stripeClient.v1().checkout().sessions()).create(paramsCaptor.capture(), optionsCaptor.capture());

        SessionCreateParams params = paramsCaptor.getValue();
        assertThat(params.getAllowedPaymentMethodTypes())
                .containsExactly(SessionCreateParams.AllowedPaymentMethodType.CARD);
        assertThat(params.getSuccessUrl()).isEqualTo(
                "http://localhost:5173/checkout/success/11?session_id={CHECKOUT_SESSION_ID}");
        assertThat(params.getCancelUrl()).isEqualTo("http://localhost:5173/checkout/cancel/11");
        assertThat(optionsCaptor.getValue().getIdempotencyKey()).isEqualTo("attempt-key");
        verify(paymentAttemptService).markSessionCreated(33L, stripeSession);
    }

    @Test
    void secondCheckoutCallReusesAnOpenSession() throws Exception {
        CheckoutPreparation first = preparation(null, null);
        CheckoutPreparation reusable = preparation("cs_test_123", "https://checkout.stripe.test/cs_test_123");
        when(paymentAttemptService.prepareCheckout(11L, null)).thenReturn(first, reusable);

        service.createCheckoutSession(11L, null);
        CheckoutSessionResponse second = service.createCheckoutSession(11L, null);

        assertThat(second.sessionId()).isEqualTo("cs_test_123");
        verify(stripeClient.v1().checkout().sessions(), times(1))
                .create(any(SessionCreateParams.class), any(RequestOptions.class));
        verify(paymentAttemptService, times(1)).markSessionCreated(33L, stripeSession);
    }

    @Test
    void completedSessionReturnsToVerificationWithoutCreatingAnotherPayment() throws Exception {
        stripeSession.setStatus("complete");
        stripeSession.setPaymentStatus("paid");
        stripeSession.setUrl(null);
        CheckoutPreparation reusable = preparation(
                "cs_test_123",
                "https://checkout.stripe.test/cs_test_123"
        );
        when(paymentAttemptService.prepareCheckout(11L, null)).thenReturn(reusable);

        CheckoutSessionResponse response = service.createCheckoutSession(11L, null);

        assertThat(response.sessionId()).isEqualTo("cs_test_123");
        assertThat(response.checkoutUrl()).isEqualTo("http://localhost:5173/checkout/success/11");
        verify(stripeClient.v1().checkout().sessions(), never())
                .create(any(SessionCreateParams.class), any(RequestOptions.class));
        verify(paymentAttemptService, never()).markSessionCreated(anyLong(), any(Session.class));
    }

    private CheckoutPreparation preparation(String sessionId, String checkoutUrl) {
        return new CheckoutPreparation(
                33L,
                11L,
                22L,
                "attempt-key",
                sessionId,
                checkoutUrl,
                2000L,
                "EUR",
                "demo@example.com",
                List.of(new CheckoutPreparation.LineItem(
                        "Camiseta Guateque Riders", "Talla M x 1", 2000L, 1L
                ))
        );
    }
}
