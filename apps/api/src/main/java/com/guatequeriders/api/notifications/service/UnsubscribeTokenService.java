package com.guatequeriders.api.notifications.service;

import com.guatequeriders.api.notifications.model.NotificationCategory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class UnsubscribeTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secret;

    public UnsubscribeTokenService(
            @Value("${app.mail.unsubscribe-secret}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("UNSUBSCRIBE_SECRET must contain at least 32 characters");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String create(Long userId, NotificationCategory category) {
        String payload = userId + ":" + category.name();
        String encodedPayload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(sign(payload));
        return encodedPayload + "." + signature;
    }

    public Long verify(String token, NotificationCategory category) {
        try {
            String[] parts = token.split("\\.", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid unsubscribe token");
            }

            String payload = new String(
                    Base64.getUrlDecoder().decode(parts[0]),
                    StandardCharsets.UTF_8
            );
            String[] payloadParts = payload.split(":", 2);
            if (payloadParts.length != 2 || !payloadParts[1].equals(category.name())) {
                throw new IllegalArgumentException("Invalid unsubscribe token");
            }

            byte[] providedSignature = Base64.getUrlDecoder().decode(parts[1]);
            if (!MessageDigest.isEqual(sign(payload), providedSignature)) {
                throw new IllegalArgumentException("Invalid unsubscribe token");
            }

            return Long.valueOf(payloadParts[0]);
        } catch (NumberFormatException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid unsubscribe token");
        }
    }

    private byte[] sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign unsubscribe token", exception);
        }
    }
}
