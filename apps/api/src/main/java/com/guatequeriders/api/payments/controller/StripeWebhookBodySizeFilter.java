package com.guatequeriders.api.payments.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "true")
public class StripeWebhookBodySizeFilter extends OncePerRequestFilter {

    private static final long MAX_PAYLOAD_BYTES = 256L * 1024L;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/api/payments/stripe/webhook".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long contentLength = request.getContentLengthLong();
        if (contentLength > MAX_PAYLOAD_BYTES) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
