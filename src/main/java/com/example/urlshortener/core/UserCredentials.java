package com.example.urlshortener.core;

import java.io.Serializable;
import java.util.UUID;

public class UserCredentials implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String passwordHash;
    private final UUID userId;
    private final UserRole role;

    public UserCredentials(String username, String passwordHash, UUID userId, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.userId = userId;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public UUID getUserId() { return userId; }
    public UserRole getRole() { return role; }
}