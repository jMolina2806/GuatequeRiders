package com.guatequeriders.api.payments.controller;

import com.guatequeriders.api.payments.config.StripeProperties;
import com.guatequeriders.api.payments.exception.DuplicateStripeEventException;
import com.guatequeriders.api.payments.service.StripeWebhookService;
import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "true")
@RequestMapping("/api/payments/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);
    private static final int MAX_PAYLOAD_BYTES = 256 * 1024;

    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;
    private final StripeWebhookService stripeWebhookService;

    public StripeWebhookController(StripeClient stripeClient,
                                   StripeProperties stripeProperties,
                                   StripeWebhookService stripeWebhookService) {
        this.stripeClient = stripeClient;
        this.stripeProperties = stripeProperties;
        this.stripeWebhookService = stripeWebhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestHeader(value = "Stripe-Signature", required = false) String signature,
            @RequestBody String payload
    ) {
        if (payload == null || payload.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_PAYLOAD_BYTES) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        }
        if (signature == null || signature.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Event event;
        try {
            event = stripeClient.constructEvent(payload, signature, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException | RuntimeException exception) {
            log.warn("Rejected Stripe webhook signature or payload");
            return ResponseEntity.badRequest().build();
        }

        if (event.getId() == null || event.getType() == null || event.getLivemode() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (event.getLivemode() != stripeProperties.isLiveMode()) {
            log.warn("Ignored Stripe event from the wrong environment: stripeEventId={}, eventType={}",
                    event.getId(), event.getType());
            return ResponseEntity.ok().build();
        }
        if (!StripeWebhookService.SUPPORTED_EVENTS.contains(event.getType())) {
            return ResponseEntity.ok().build();
        }

        Optional<StripeObject> object = event.getDataObjectDeserializer().getObject();
        if (object.isEmpty() || !(object.get() instanceof Session session)) {
            log.warn("Rejected Stripe event with an unsupported Checkout payload: stripeEventId={}, eventType={}",
                    event.getId(), event.getType());
            return ResponseEntity.badRequest().build();
        }

        try {
            stripeWebhookService.process(event, session);
            return ResponseEntity.ok().build();
        } catch (DuplicateStripeEventException exception) {
            return ResponseEntity.ok().build();
        }
    }
}
