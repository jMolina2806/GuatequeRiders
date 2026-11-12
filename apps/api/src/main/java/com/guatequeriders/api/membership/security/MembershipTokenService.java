package com.guatequeriders.api.membership.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

@Service
public class MembershipTokenService {

    private static final String PURPOSE = "membership-verification";
    private final SecretKey secretKey;
    private final long expirationSeconds;

    public MembershipTokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${app.membership.verification-expiration-seconds:90}") long expirationSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        if (expirationSeconds < 30) {
            throw new IllegalArgumentException("Membership code lifetime must be at least 30 seconds");
        }
        this.expirationSeconds = expirationSeconds;
    }

    public IssuedVerification issue(Long userId, long membershipVersion) {
        Instant now = Instant.now();
        long period = Math.floorDiv(now.getEpochSecond(), expirationSeconds);
        Instant expiresAt = Instant.ofEpochSecond((period + 1) * expirationSeconds);
        String code = generateCode(userId, membershipVersion, period);

        String token = Jwts.builder()
                .subject(PURPOSE)
                .claim("purpose", PURPOSE)
                .claim("uid", userId)
                .claim("mv", membershipVersion)
                .claim("code", code)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();

        return new IssuedVerification(token, code, expiresAt);
    }

    public boolean matchesCode(Long userId, long membershipVersion, String providedCode) {
        if (providedCode == null || !providedCode.matches("\\d{6}")) {
            return false;
        }
        long period = Math.floorDiv(Instant.now().getEpochSecond(), expirationSeconds);
        byte[] expected = generateCode(userId, membershipVersion, period).getBytes(StandardCharsets.UTF_8);
        byte[] received = providedCode.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, received);
    }

    public Instant currentCodeExpiration() {
        long period = Math.floorDiv(Instant.now().getEpochSecond(), expirationSeconds);
        return Instant.ofEpochSecond((period + 1) * expirationSeconds);
    }

    private String generateCode(Long userId, long membershipVersion, long period) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] digest = mac.doFinal(
                    ("member-code:" + userId + ":" + membershipVersion + ":" + period)
                            .getBytes(StandardCharsets.UTF_8));
            int offset = digest[digest.length - 1] & 0x0f;
            int value = ((digest[offset] & 0x7f) << 24)
                    | ((digest[offset + 1] & 0xff) << 16)
                    | ((digest[offset + 2] & 0xff) << 8)
                    | (digest[offset + 3] & 0xff);
            return String.format(Locale.ROOT, "%06d", value % 1_000_000);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to generate member verification code", ex);
        }
    }

    public VerificationClaims parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!PURPOSE.equals(claims.get("purpose", String.class))
                    || !PURPOSE.equals(claims.getSubject())) {
                throw unavailable();
            }

            Object userIdValue = claims.get("uid");
            Object versionValue = claims.get("mv");
            String code = claims.get("code", String.class);

            if (!(userIdValue instanceof Number userId)
                    || !(versionValue instanceof Number version)
                    || code == null) {
                throw unavailable();
            }

            return new VerificationClaims(
                    userId.longValue(),
                    version.longValue(),
                    code,
                    claims.getExpiration().toInstant()
            );
        } catch (JwtException | IllegalArgumentException e) {
            throw unavailable();
        }
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification unavailable");
    }

    public record IssuedVerification(String token, String code, Instant expiresAt) {
    }

    public record VerificationClaims(Long userId, long membershipVersion, String code, Instant expiresAt) {
    }
}
