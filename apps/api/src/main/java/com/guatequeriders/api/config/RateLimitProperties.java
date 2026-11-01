package com.guatequeriders.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private int loginPerMinute = 10;
    private int registerPerMinute = 5;
    private int checkoutSessionPerMinute = 10;
    private int uploadPerMinute = 30;
    private int unsubscribePerMinute = 10;

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

    public int getRegisterPerMinute() {
        return registerPerMinute;
    }

    public void setRegisterPerMinute(int registerPerMinute) {
        this.registerPerMinute = registerPerMinute;
    }

    public int getCheckoutSessionPerMinute() {
        return checkoutSessionPerMinute;
    }

    public void setCheckoutSessionPerMinute(int checkoutSessionPerMinute) {
        this.checkoutSessionPerMinute = checkoutSessionPerMinute;
    }

    public int getUploadPerMinute() {
        return uploadPerMinute;
    }

    public void setUploadPerMinute(int uploadPerMinute) {
        this.uploadPerMinute = uploadPerMinute;
    }

    public int getUnsubscribePerMinute() {
        return unsubscribePerMinute;
    }

    public void setUnsubscribePerMinute(int unsubscribePerMinute) {
        this.unsubscribePerMinute = unsubscribePerMinute;
    }
}
