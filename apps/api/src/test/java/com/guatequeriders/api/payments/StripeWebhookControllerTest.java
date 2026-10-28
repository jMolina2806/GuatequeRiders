package com.guatequeriders.api.payments;

import com.guatequeriders.api.payments.config.StripeMode;
import com.guatequeriders.api.payments.config.StripeProperties;
import com.guatequeriders.api.payments.controller.StripeWebhookController;
import com.guatequeriders.api.payments.service.StripeWebhookService;
import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class StripeWebhookControllerTest {

    private StripeClient stripeClient;
    private StripeWebhookService stripeWebhookService;
    private StripeWebhookController controller;

    @BeforeEach
    void setUp() {
        stripeClient = mock(StripeClient.class);
        stripeWebhookService = mock(StripeWebhookService.class);
        StripeProperties properties = new StripeProperties();
        properties.setEnabled(true);
        properties.setMode(StripeMode.TEST);
        properties.setWebhookSecret("whsec_test");
        controller = new StripeWebhookController(stripeClient, properties, stripeWebhookService);
    }

    @Test
    void invalidSignatureReturnsBadRequestWithoutDatabaseProcessing() throws Exception {
        when(stripeClient.constructEvent("{}", "bad-signature", "whsec_test"))
                .thenThrow(new SignatureVerificationException("invalid", "bad-signature"));

        var response = controller.webhook("bad-signature", "{}");

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        verifyNoInteractions(stripeWebhookService);
    }

    @Test
    void wrongLivemodeIsIgnoredBeforePayloadProcessing() throws Exception {
        Event event = new Event();
        event.setId("evt_live");
        event.setType("checkout.session.completed");
        event.setLivemode(true);
        when(stripeClient.constructEvent("{}", "valid-signature", "whsec_test"))
                .thenReturn(event);

        var response = controller.webhook("valid-signature", "{}");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verifyNoInteractions(stripeWebhookService);
    }
}
