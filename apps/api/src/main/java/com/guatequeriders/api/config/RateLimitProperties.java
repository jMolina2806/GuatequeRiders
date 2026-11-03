package com.guatequeriders.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private int loginPerMinute = 60;
    private int checkoutSessionPerMinute = 20;
    private int membershipVerificationPerMinute = 60;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getLoginPerMinute() {
        return loginPerMinute;
    }

    public void setLoginPerMinute(int loginPerMinute) {
        this.loginPerMinute = loginPerMinute;
    }

    public int getCheckoutSessionPerMinute() {
        return checkoutSessionPerMinute;
    }

    public void setCheckoutSessionPerMinute(int checkoutSessionPerMinute) {
        this.checkoutSessionPerMinute = checkoutSessionPerMinute;
    }

    public int getMembershipVerificationPerMinute() {
        return membershipVerificationPerMinute;
    }

    public void setMembershipVerificationPerMinute(int membershipVerificationPerMinute) {
        this.membershipVerificationPerMinute = membershipVerificationPerMinute;
    }
}
