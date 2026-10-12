package com.fakenews.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RateLimiterService Unit Tests")
class RateLimiterServiceTest {

    @Test
    @DisplayName("Should allow requests within limit and throttle when limit is exceeded")
    void testRateLimiterThrottle() {
        RateLimiterService limiter = new RateLimiterService();
        String testKey = "client-ip-127.0.0.1";

        // First 30 requests should succeed
        for (int i = 0; i < 30; i++) {
            assertTrue(limiter.tryAcquire(testKey), "Request " + (i + 1) + " should be allowed");
        }

        // 31st request should be rejected
        assertFalse(limiter.tryAcquire(testKey), "Request 31 should be throttled");

        // A different key should still have its quota
        assertTrue(limiter.tryAcquire("different-client-ip"), "Different client should be permitted");
    }
}
