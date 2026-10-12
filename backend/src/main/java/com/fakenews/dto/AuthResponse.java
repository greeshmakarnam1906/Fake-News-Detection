package com.fakenews.dto;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String name;
    private String email;
    private String role;
    private long expiresInMs;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long userId, String name, String email, String role, long expiresInMs) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.expiresInMs = expiresInMs;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public void setExpiresInMs(long expiresInMs) {
        this.expiresInMs = expiresInMs;
    }
}
