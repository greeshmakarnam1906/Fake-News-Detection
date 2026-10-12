package com.fakenews.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory sliding-window rate limiter for expensive analysis inference requests.
 * Default limit: 30 requests per minute per client key (IP or user ID).
 */
@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS_PER_WINDOW = 30;
    private static final long WINDOW_DURATION_SECONDS = 60;

    private static class ClientBucket {
        int count;
        long windowStartSeconds;

        ClientBucket(long now) {
            this.count = 1;
            this.windowStartSeconds = now;
        }
    }

    private final Map<String, ClientBucket> buckets = new ConcurrentHashMap<>();

    public synchronized boolean tryAcquire(String clientKey) {
        long now = Instant.now().getEpochSecond();
        ClientBucket bucket = buckets.get(clientKey);

        if (bucket == null || now - bucket.windowStartSeconds >= WINDOW_DURATION_SECONDS) {
            buckets.put(clientKey, new ClientBucket(now));
            return true;
        }

        if (bucket.count < MAX_REQUESTS_PER_WINDOW) {
            bucket.count++;
            return true;
        }

        return false;
    }
}
