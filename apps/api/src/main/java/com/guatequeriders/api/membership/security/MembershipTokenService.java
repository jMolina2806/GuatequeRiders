package com.guatequeriders.api.membership.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;

@Service
public class MembershipTokenService {

    private static final String PURPOSE = "membership-verification";
    private final SecretKey secretKey;
    private final long expirationSeconds;
    private final SecureRandom secureRandom = new SecureRandom();

    public MembershipTokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${app.membership.verification-expiration-seconds:90}") long expirationSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public IssuedVerification issue(Long userId, long membershipVersion) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

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
