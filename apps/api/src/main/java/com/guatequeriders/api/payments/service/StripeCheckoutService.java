package com.guatequeriders.api.payments.service;

import com.guatequeriders.api.payments.config.StripeProperties;
import com.guatequeriders.api.payments.dto.CheckoutSessionResponse;
import com.guatequeriders.api.payments.exception.PaymentProviderException;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "true")
public class StripeCheckoutService {

    private static final Logger log = LoggerFactory.getLogger(StripeCheckoutService.class);
    private static final String CURRENCY = "eur";

    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;
    private final PaymentAttemptService paymentAttemptService;

    public StripeCheckoutService(StripeClient stripeClient,
                                 StripeProperties stripeProperties,
                                 PaymentAttemptService paymentAttemptService) {
        this.stripeClient = stripeClient;
        this.stripeProperties = stripeProperties;
        this.paymentAttemptService = paymentAttemptService;
    }

    public CheckoutSessionResponse createCheckoutSession(
            Long orderId,
        com.guatequeriders.api.users.model.User user
    ) {
        CheckoutPreparation preparation = paymentAttemptService.prepareCheckout(orderId, user);
        if (preparation.hasSession()) {
            CheckoutSessionResponse reused = retrieveExistingSession(preparation);
            if (reused != null) {
                return reused;
            }
            preparation = paymentAttemptService.prepareCheckout(orderId, user);
        }

        Session session = createStripeSession(preparation);
        validateSession(session, preparation);
        paymentAttemptService.markSessionCreated(preparation.paymentAttemptId(), session);
        log.info("Stripe Checkout Session created: orderId={}, paymentAttemptId={}, stripeSessionId={}",
                preparation.orderId(), preparation.paymentAttemptId(), session.getId());
        return new CheckoutSessionResponse(session.getUrl(), session.getId());
    }

    private CheckoutSessionResponse retrieveExistingSession(CheckoutPreparation preparation) {
        try {
            Session session = stripeClient.v1().checkout().sessions()
                    .retrieve(preparation.sessionId());
            validateLivemode(session);
            if ("expired".equalsIgnoreCase(session.getStatus())) {
                paymentAttemptService.markExpired(preparation.paymentAttemptId());
                return null;
            }
            validateSession(session, preparation);
            if (session.getUrl() != null && !session.getUrl().isBlank()) {
                paymentAttemptService.updateCheckoutUrl(preparation.paymentAttemptId(), session.getUrl());
                return new CheckoutSessionResponse(session.getUrl(), session.getId());
            }
            if (preparation.hasReusableUrl()) {
                return new CheckoutSessionResponse(preparation.checkoutUrl(), preparation.sessionId());
            }
            throw new PaymentProviderException("Stripe Checkout Session has no redirect URL", null);
        } catch (StripeException exception) {
            log.warn("Unable to verify existing Stripe Checkout Session for paymentAttemptId={}",
                    preparation.paymentAttemptId());
            throw new PaymentProviderException("Payment provider unavailable", exception);
        }
    }

    private Session createStripeSession(CheckoutPreparation preparation) {
        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .addAllowedPaymentMethodType(SessionCreateParams.AllowedPaymentMethodType.CARD)
                .setCustomerEmail(preparation.customerEmail())
                .setClientReferenceId(String.valueOf(preparation.orderId()))
                .setSuccessUrl(successUrl(preparation.orderId()))
                .setCancelUrl(cancelUrl(preparation.orderId()))
                .putMetadata("order_id", String.valueOf(preparation.orderId()))
                .putMetadata("campaign_id", String.valueOf(preparation.campaignId()))
                .putMetadata("payment_attempt_id", String.valueOf(preparation.paymentAttemptId()));

        preparation.lineItems().forEach(item -> builder.addLineItem(
                SessionCreateParams.LineItem.builder()
                        .setQuantity(item.quantity())
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(CURRENCY)
                                .setUnitAmount(item.unitAmountMinor())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(item.name())
                                        .setDescription(item.description())
                                        .build())
                                .build())
                        .build()
        ));

        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey(preparation.idempotencyKey())
                .build();
        try {
            return stripeClient.v1().checkout().sessions().create(builder.build(), requestOptions);
        } catch (StripeException exception) {
            log.warn("Stripe Checkout Session creation failed for paymentAttemptId={}",
                    preparation.paymentAttemptId());
            throw new PaymentProviderException("Payment provider unavailable", exception);
        }
    }

    private void validateSession(Session session, CheckoutPreparation preparation) {
        validateLivemode(session);
        if (session.getId() == null || session.getUrl() == null || session.getUrl().isBlank()) {
            throw new PaymentProviderException("Stripe returned an incomplete Checkout Session", null);
        }
        if (!"payment".equalsIgnoreCase(session.getMode())) {
            throw new PaymentProviderException("Stripe returned an unexpected Checkout mode", null);
        }
        if (!Long.valueOf(preparation.amountMinor()).equals(session.getAmountTotal())) {
            throw new PaymentProviderException("Stripe returned an unexpected Checkout amount", null);
        }
        if (!CURRENCY.equalsIgnoreCase(session.getCurrency())) {
            throw new PaymentProviderException("Stripe returned an unexpected Checkout currency", null);
        }
        Map<String, String> metadata = session.getMetadata();
        if (!String.valueOf(preparation.orderId()).equals(metadata == null ? null : metadata.get("order_id"))
                || !String.valueOf(preparation.campaignId())
                .equals(metadata == null ? null : metadata.get("campaign_id"))
                || !String.valueOf(preparation.paymentAttemptId())
                .equals(metadata == null ? null : metadata.get("payment_attempt_id"))) {
            throw new PaymentProviderException("Stripe returned inconsistent Checkout metadata", null);
        }
        String paymentIntentId = session.getPaymentIntent();
        if (paymentAttemptService.isPaymentIntentUsedByAnotherAttempt(
                paymentIntentId, preparation.paymentAttemptId())) {
            throw new PaymentProviderException("Stripe returned a conflicting PaymentIntent", null);
        }
    }

    private void validateLivemode(Session session) {
        if (session.getLivemode() == null || session.getLivemode() != stripeProperties.isLiveMode()) {
            throw new PaymentProviderException("Stripe environment mismatch", null);
        }
    }

    private String successUrl(Long orderId) {
        return stripeProperties.getPublicUrl()
                + "/checkout/success/" + orderId + "?session_id={CHECKOUT_SESSION_ID}";
    }

    private String cancelUrl(Long orderId) {
        return stripeProperties.getPublicUrl() + "/checkout/cancel/" + orderId;
    }
}
