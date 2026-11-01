package com.guatequeriders.api.auth.security;

import com.guatequeriders.api.common.exception.TooManyRequestsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILURES = 10;
    private static final long WINDOW_SECONDS = 15 * 60L;

    private final Map<String, FailureWindow> failures = new ConcurrentHashMap<>();

    public void assertAllowed(String username, String clientIp) {
        String key = key(username, clientIp);
        FailureWindow window = failures.get(key);
        if (window == null) {
            return;
        }

        long now = Instant.now().getEpochSecond();
        long elapsed = now - window.startedAt();
        if (elapsed >= WINDOW_SECONDS) {
            failures.remove(key, window);
            return;
        }

        if (window.count() >= MAX_FAILURES) {
            throw new TooManyRequestsException(WINDOW_SECONDS - elapsed);
        }
    }

    public void recordFailure(String username, String clientIp) {
        String key = key(username, clientIp);
        long now = Instant.now().getEpochSecond();

        failures.compute(key, (ignored, previous) -> {
            if (previous == null || now - previous.startedAt() >= WINDOW_SECONDS) {
                return new FailureWindow(now, 1);
            }
            return new FailureWindow(previous.startedAt(), previous.count() + 1);
        });

        if (failures.size() > 10_000) {
            failures.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= WINDOW_SECONDS);
        }
    }

    public void reset(String username, String clientIp) {
        failures.remove(key(username, clientIp));
    }

    private String key(String username, String clientIp) {
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        String normalizedIp = clientIp == null || clientIp.isBlank() ? "unknown" : clientIp;
        return normalizedUsername + ":" + normalizedIp;
    }

    private record FailureWindow(long startedAt, int count) {
    }
}
