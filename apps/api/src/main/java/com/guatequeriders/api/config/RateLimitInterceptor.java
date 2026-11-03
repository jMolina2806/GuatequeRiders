package com.guatequeriders.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guatequeriders.api.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final long WINDOW_SECONDS = 60;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitInterceptor(RateLimitProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!properties.isEnabled()) {
            return true;
        }

        int limit = limitFor(request.getMethod(), request.getRequestURI());
        if (limit < 0) {
            return true;
        }

        String key = request.getRequestURI() + ":" + clientKey(request);
        long now = Instant.now().getEpochSecond();
        AtomicBoolean limited = new AtomicBoolean(false);
        windows.compute(key, (ignored, previous) -> {
            if (previous == null || now - previous.startedAt() >= WINDOW_SECONDS) {
                return new Window(now, 1);
            }
            if (previous.count() >= limit) {
                limited.set(true);
                return previous;
            }
            return new Window(previous.startedAt(), previous.count() + 1);
        });

        if (limited.get()) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(WINDOW_SECONDS));
            response.setContentType("application/json");
            objectMapper.writeValue(response.getOutputStream(),
                    new ErrorResponse(429, "Too many requests"));
            return false;
        }

        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= WINDOW_SECONDS);
        }
        return true;
    }

    private int limitFor(String method, String path) {
        if ("POST".equalsIgnoreCase(method) && "/api/auth/login".equals(path)) {
            return properties.getLoginPerMinute();
        }
        if ("POST".equalsIgnoreCase(method) && path.matches("/api/orders/\\d+/checkout-session")) {
            return properties.getCheckoutSessionPerMinute();
        }
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/membership/verify/")) {
            return properties.getMembershipVerificationPerMinute();
        }
        return -1;
    }

    private String clientKey(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
        if (request.getUserPrincipal() == null) {
            return remoteAddress;
        }
        return remoteAddress + ":" + request.getUserPrincipal().getName();
    }

    private record Window(long startedAt, int count) {
    }
}
