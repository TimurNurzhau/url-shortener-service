package com.example.urlshortener.core;

import java.time.Instant;
import java.util.UUID;

public class Link {
    private final String originalUrl;
    private final String shortCode;
    private final UUID ownerId;
    private final int clickLimit;
    private int currentClicks;
    private final Instant creationTime;
    private final Instant expirationTime;

    public Link(String originalUrl, String shortCode, UUID ownerId, int clickLimit, Instant expirationTime) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.ownerId = ownerId;
        this.clickLimit = clickLimit;
        this.currentClicks = 0;
        this.creationTime = Instant.now();
        this.expirationTime = expirationTime;
    }

    // Геттеры для всех полей
    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public int getClickLimit() {
        return clickLimit;
    }

    public int getCurrentClicks() {
        return currentClicks;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    // Метод для увеличения счетчика кликов
    public void incrementClicks() {
        this.currentClicks++;
    }
}