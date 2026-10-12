package com.fakenews.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "TestSecretKeyForFakeNewsDetectionUnitTestingAtLeast32Bytes!";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(secret, expirationMs);
    }

    @Test
    @DisplayName("Should generate a valid JWT and extract correct claims")
    void testGenerateAndParseToken() {
        String token = tokenProvider.generateToken(42L, "reporter@example.com", "ROLE_USER");

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(42L, tokenProvider.getUserIdFromJwt(token));
        assertEquals("reporter@example.com", tokenProvider.getEmailFromJwt(token));
    }

    @Test
    @DisplayName("Should reject malformed or tampered JWT")
    void testRejectInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid.jwt.token"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }
}
