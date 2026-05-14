package com.security.learn.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, Window> cache = new ConcurrentHashMap<>();

    public boolean isAllowed(String clientIp) {
        Window window = cache.get(clientIp);
        Instant now = Instant.now();

        if (window == null || window.startTime.plusSeconds(WINDOW_SECONDS).isBefore(now)) {
            cache.put(clientIp, new Window(now, 1));
            return true;
        }

        if (window.count >= MAX_ATTEMPTS) {
            return false;
        }

        window.count++;
        return true;
    }

    private static class Window {
        final Instant startTime;
        int count;

        Window(Instant startTime, int count) {
            this.startTime = startTime;
            this.count = count;
        }
    }
}
