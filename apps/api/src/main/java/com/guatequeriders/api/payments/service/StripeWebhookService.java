package com.guatequeriders.api.payments.service;

import com.guatequeriders.api.orders.service.OrdersService;
import com.guatequeriders.api.payments.config.StripeProperties;
import com.guatequeriders.api.payments.exception.DuplicateStripeEventException;
import com.guatequeriders.api.payments.model.PaymentApplicationResult;
import com.guatequeriders.api.payments.model.PaymentAttempt;
import com.guatequeriders.api.payments.model.StripeEvent;
import com.guatequeriders.api.payments.model.StripeEventStatus;
import com.guatequeriders.api.payments.repository.StripeEventRepository;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class StripeWebhookService {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookService.class);

    public static final Set<String> SUPPORTED_EVENTS = Set.of(
            "checkout.session.completed",
            "checkout.session.async_payment_succeeded",
            "checkout.session.async_payment_failed",
            "checkout.session.expired"
    );

    private final StripeEventRepository stripeEventRepository;
    private final PaymentAttemptService paymentAttemptService;
    private final OrdersService ordersService;
    private final StripeProperties stripeProperties;

    public StripeWebhookService(StripeEventRepository stripeEventRepository,
                                PaymentAttemptService paymentAttemptService,
                                OrdersService ordersService,
                                StripeProperties stripeProperties) {
        this.stripeEventRepository = stripeEventRepository;
        this.paymentAttemptService = paymentAttemptService;
        this.ordersService = ordersService;
        this.stripeProperties = stripeProperties;
    }

    @Transactional
    public void process(Event event, Session session) {
        if (event.getLivemode() == null || event.getLivemode() != stripeProperties.isLiveMode()) {
            log.warn("Ignored Stripe event due to environment mismatch: stripeEventId={}, eventType={}",
                    event.getId(), event.getType());
            return;
        }

        if (stripeEventRepository.findByStripeEventId(event.getId()).isPresent()) {
            log.info("Ignored duplicate Stripe event: stripeEventId={}, eventType={}",
                    event.getId(), event.getType());
            return;
        }

        StripeEvent stripeEvent = new StripeEvent();
        stripeEvent.setStripeEventId(event.getId());
        stripeEvent.setEventType(event.getType());
        stripeEvent.setObjectId(session.getId());
        stripeEvent.setLivemode(Boolean.TRUE.equals(event.getLivemode()));
        stripeEvent.setStatus(StripeEventStatus.RECEIVED);
        try {
            stripeEventRepository.saveAndFlush(stripeEvent);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateStripeEventException();
        }
        stripeEvent.setStatus(StripeEventStatus.PROCESSING);
        stripeEventRepository.save(stripeEvent);

        PaymentAttempt attempt = paymentAttemptService.findBySessionId(session.getId());
        if (attempt == null) {
            fail(stripeEvent, "UNKNOWN_CHECKOUT_SESSION");
            return;
        }
        stripeEvent.setPaymentAttempt(attempt);

        ValidationResult validation = validateSession(session, attempt);
        if (!validation.valid()) {
            fail(stripeEvent, validation.failureReason());
            return;
        }

        String eventType = event.getType();
        if ("checkout.session.completed".equals(eventType)
                && !"paid".equalsIgnoreCase(session.getPaymentStatus())) {
            markProcessed(stripeEvent);
            log.info("Stripe Checkout completed without paid status: stripeEventId={}, eventType={}, orderId={}, paymentAttemptId={}",
                    event.getId(), eventType, attempt.getOrder().getId(), attempt.getId());
            return;
        }
        if ("checkout.session.async_payment_succeeded".equals(eventType)
                && !"paid".equalsIgnoreCase(session.getPaymentStatus())) {
            markProcessed(stripeEvent);
            return;
        }

        if ("checkout.session.async_payment_failed".equals(eventType)) {
            paymentAttemptService.markFailed(attempt.getId());
            markProcessed(stripeEvent);
            log.info("Stripe asynchronous payment failed: stripeEventId={}, eventType={}, orderId={}, paymentAttemptId={}",
                    event.getId(), eventType, attempt.getOrder().getId(), attempt.getId());
            return;
        }
        if ("checkout.session.expired".equals(eventType)) {
            paymentAttemptService.markExpired(attempt.getId());
            markProcessed(stripeEvent);
            log.info("Stripe Checkout session expired: stripeEventId={}, eventType={}, orderId={}, paymentAttemptId={}",
                    event.getId(), eventType, attempt.getOrder().getId(), attempt.getId());
            return;
        }

        PaymentApplicationResult result = ordersService.applyVerifiedPaymentWebhook(
                attempt.getOrder().getId(),
                attempt.getId(),
                session.getId(),
                validation.paymentIntentId(),
                validation.amountMinor(),
                validation.currency()
        );
        if (result == PaymentApplicationResult.PAID || result == PaymentApplicationResult.ALREADY_PAID) {
            markProcessed(stripeEvent);
            log.info("Stripe payment applied: stripeEventId={}, eventType={}, orderId={}, paymentAttemptId={}, result={}",
                    event.getId(), eventType, attempt.getOrder().getId(), attempt.getId(), result);
            return;
        }

        if (result == PaymentApplicationResult.ORDER_CANCELLED) {
            paymentAttemptService.markFailed(attempt.getId());
            fail(stripeEvent, "ORDER_CANCELLED_REFUND_REQUIRED");
            log.warn("Stripe payment arrived for a cancelled order: stripeEventId={}, eventType={}, orderId={}, paymentAttemptId={}",
                    event.getId(), eventType, attempt.getOrder().getId(), attempt.getId());
            return;
        }

        fail(stripeEvent, "INVALID_PAYMENT_ATTEMPT_STATE");
        log.warn("Stripe payment did not match the internal attempt state: stripeEventId={}, eventType={}, orderId={}, paymentAttemptId={}",
                event.getId(), eventType, attempt.getOrder().getId(), attempt.getId());
    }

    private ValidationResult validateSession(Session session, PaymentAttempt attempt) {
        if (session.getLivemode() == null || session.getLivemode() != stripeProperties.isLiveMode()) {
            return ValidationResult.invalid("ENVIRONMENT_MISMATCH");
        }
        if (session.getId() == null || !session.getId().equals(attempt.getStripeCheckoutSessionId())) {
            return ValidationResult.invalid("SESSION_ID_MISMATCH");
        }
        if (!"payment".equalsIgnoreCase(session.getMode())) {
            return ValidationResult.invalid("SESSION_MODE_MISMATCH");
        }

        Map<String, String> metadata = session.getMetadata();
        if (metadata == null
                || !String.valueOf(attempt.getId()).equals(metadata.get("payment_attempt_id"))
                || !String.valueOf(attempt.getOrder().getId()).equals(metadata.get("order_id"))
                || !String.valueOf(attempt.getOrder().getCampaign().getId()).equals(metadata.get("campaign_id"))
                || !String.valueOf(attempt.getOrder().getId()).equals(session.getClientReferenceId())) {
            return ValidationResult.invalid("METADATA_MISMATCH");
        }

        if (session.getAmountTotal() == null
                || !attempt.getAmountMinor().equals(session.getAmountTotal())) {
            return ValidationResult.invalid("AMOUNT_MISMATCH");
        }
        if (session.getCurrency() == null
                || !attempt.getCurrency().equalsIgnoreCase(session.getCurrency())) {
            return ValidationResult.invalid("CURRENCY_MISMATCH");
        }

        String paymentIntentId = session.getPaymentIntent();
        if (paymentAttemptService.isPaymentIntentUsedByAnotherAttempt(paymentIntentId, attempt.getId())) {
            return ValidationResult.invalid("PAYMENT_INTENT_REUSED");
        }
        if (paymentIntentId != null && attempt.getStripePaymentIntentId() != null
                && !paymentIntentId.equals(attempt.getStripePaymentIntentId())) {
            return ValidationResult.invalid("PAYMENT_INTENT_MISMATCH");
        }

        return ValidationResult.valid(
                session.getAmountTotal(),
                session.getCurrency().toUpperCase(Locale.ROOT),
                paymentIntentId
        );
    }

    private void markProcessed(StripeEvent stripeEvent) {
        stripeEvent.setStatus(StripeEventStatus.PROCESSED);
        stripeEvent.setProcessedAt(LocalDateTime.now());
        stripeEvent.setFailureReason(null);
        stripeEventRepository.save(stripeEvent);
    }

    private void fail(StripeEvent stripeEvent, String reason) {
        stripeEvent.setStatus(StripeEventStatus.FAILED);
        stripeEvent.setFailureReason(reason);
        stripeEvent.setProcessedAt(LocalDateTime.now());
        stripeEventRepository.save(stripeEvent);
    }

    private record ValidationResult(
            boolean valid,
            String failureReason,
            long amountMinor,
            String currency,
            String paymentIntentId
    ) {
        static ValidationResult valid(long amountMinor, String currency, String paymentIntentId) {
            return new ValidationResult(true, null, amountMinor, currency, paymentIntentId);
        }

        static ValidationResult invalid(String failureReason) {
            return new ValidationResult(false, failureReason, 0, "", null);
        }
    }
}
