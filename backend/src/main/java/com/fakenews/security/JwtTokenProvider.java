package com.fakenews.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey key;
    private final long jwtExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:defaultSecretKeyForDevelopmentMustBe32BytesLongMin!}") String jwtSecret,
            @Value("${app.jwt.expiration-ms:86400000}") long jwtExpirationMs) {

        // Ensure minimum 256-bit key length
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            this.key = Keys.hmacShaKeyFor(padded);
        } else {
            this.key = Keys.hmacShaKeyFor(keyBytes);
        }
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromJwt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    public String getEmailFromJwt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("email", String.class);
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            logger.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}
