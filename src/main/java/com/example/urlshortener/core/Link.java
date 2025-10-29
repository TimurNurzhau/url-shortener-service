package com.example.urlshortener.core;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Link implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String originalUrl;
    private final String shortCode;
    private final UUID ownerId;
    private final int clickLimit;
    private final AtomicInteger currentClicks; // Изменено на AtomicInteger
    private final Instant creationTime;
    private final Instant expirationTime;

    public Link(
            String originalUrl,
            String shortCode,
            UUID ownerId,
            int clickLimit,
            Instant expirationTime) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.ownerId = ownerId;
        this.clickLimit = clickLimit;
        this.currentClicks = new AtomicInteger(0); // Инициализация AtomicInteger
        this.creationTime = Instant.now();
        this.expirationTime = expirationTime;
    }

    // Геттеры остаются прежними, кроме getCurrentClicks
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
        return currentClicks.get();
    } // Получение значения

    public Instant getCreationTime() {
        return creationTime;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    // Атомарное увеличение счетчика
    public void incrementClicks() {
        this.currentClicks.incrementAndGet();
    }
}
