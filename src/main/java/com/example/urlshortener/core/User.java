package com.example.urlshortener.core;

import java.util.UUID;

public class User {
    private final UUID id;
    private final String username;
    private final UserRole role;

    public User(UUID id, String username, UserRole role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    // Геттеры
    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
