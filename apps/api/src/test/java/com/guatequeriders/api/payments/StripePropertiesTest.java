package com.guatequeriders.api.payments;

import com.guatequeriders.api.payments.config.StripeMode;
import com.guatequeriders.api.payments.config.StripeProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StripePropertiesTest {

    @Test
    void disabledStripeDoesNotRequireSecrets() {
        StripeProperties properties = new StripeProperties();
        properties.setEnabled(false);

        assertThatCode(() -> properties.validate("production")).doesNotThrowAnyException();
    }

    @Test
    void testModeRejectsLiveSecret() {
        StripeProperties properties = properties(true, StripeMode.TEST, "sk_live_secret", "http://localhost:5173");

        assertThatThrownBy(() -> properties.validate("development"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    void liveModeRequiresProductionAndHttps() {
        StripeProperties properties = properties(true, StripeMode.LIVE, "sk_live_secret", "http://localhost:5173");

        assertThatThrownBy(() -> properties.validate("development"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_ENV=production");

        assertThatThrownBy(() -> properties.validate("production"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("HTTPS");
    }

    @Test
    void testModeAcceptsLocalHttpUrlAndMatchingSecrets() {
        StripeProperties properties = properties(true, StripeMode.TEST, "sk_test_secret", "http://localhost:5173");

        assertThatCode(() -> properties.validate("development")).doesNotThrowAnyException();
    }

    private StripeProperties properties(boolean enabled,
                                        StripeMode mode,
                                        String secretKey,
                                        String publicUrl) {
        StripeProperties properties = new StripeProperties();
        properties.setEnabled(enabled);
        properties.setMode(mode);
        properties.setSecretKey(secretKey);
        properties.setWebhookSecret("whsec_test");
        properties.setPublicUrl(publicUrl);
        return properties;
    }
}
