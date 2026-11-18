package com.guatequeriders.api.payments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    private boolean enabled;
    private StripeMode mode = StripeMode.TEST;
    private String secretKey = "";
    private String webhookSecret = "";
    private String publicUrl = "";

    public void validate(String applicationEnvironment) {
        if (!enabled) {
            return;
        }
        if (mode == null) {
            throw new IllegalStateException("STRIPE_MODE must be test or live");
        }
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("STRIPE_SECRET_KEY is required when STRIPE_ENABLED=true");
        }
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("STRIPE_WEBHOOK_SECRET is required when STRIPE_ENABLED=true");
        }
        if (publicUrl == null || publicUrl.isBlank()) {
            throw new IllegalStateException("APP_PUBLIC_URL is required when STRIPE_ENABLED=true");
        }
        if (!webhookSecret.startsWith("whsec_")) {
            throw new IllegalStateException("STRIPE_WEBHOOK_SECRET must start with whsec_");
        }

        String expectedPrefix = mode == StripeMode.TEST ? "sk_test_" : "sk_live_";
        if (!secretKey.startsWith(expectedPrefix)) {
            throw new IllegalStateException("STRIPE_SECRET_KEY does not match STRIPE_MODE");
        }
        if (mode == StripeMode.LIVE && !"production".equalsIgnoreCase(applicationEnvironment)) {
            throw new IllegalStateException("Stripe LIVE mode requires APP_ENV=production");
        }

        URI uri;
        try {
            uri = URI.create(normalizedPublicUrl());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("APP_PUBLIC_URL must be a valid absolute URL", exception);
        }
        if (uri.getScheme() == null || uri.getHost() == null
                || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalStateException("APP_PUBLIC_URL must be an origin or path without query parameters");
        }
        if (mode == StripeMode.LIVE && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalStateException("APP_PUBLIC_URL must use HTTPS in Stripe LIVE mode");
        }
        if (mode == StripeMode.TEST
                && !"http".equalsIgnoreCase(uri.getScheme())
                && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalStateException("APP_PUBLIC_URL must use HTTP or HTTPS in Stripe TEST mode");
        }
    }

    public String normalizedPublicUrl() {
        if (publicUrl == null) {
            return "";
        }
        return publicUrl.trim().replaceFirst("/+$", "");
    }

    public boolean isLiveMode() {
        return mode == StripeMode.LIVE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public StripeMode getMode() {
        return mode;
    }

    public void setMode(StripeMode mode) {
        this.mode = mode;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public String getPublicUrl() {
        return normalizedPublicUrl();
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }
}
