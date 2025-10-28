package com.urlshortener.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ShortLink {
    private final String id;
    private final String originalUrl;
    private final String shortCode;
    private final UUID userId;
    private final LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private int clickCount;
    private int maxClicks;
    private boolean isActive;

    public ShortLink(String originalUrl, String shortCode, UUID userId, int maxClicks, int ttlHours) {
        this.id = UUID.randomUUID().toString();
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusHours(ttlHours);
        this.clickCount = 0;
        this.maxClicks = maxClicks;
        this.isActive = true;
    }

    public String getId() { return id; }
    public String getOriginalUrl() { return originalUrl; }
    public String getShortCode() { return shortCode; }
    public UUID getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public int getClickCount() { return clickCount; }
    public int getMaxClicks() { return maxClicks; }
    public boolean isActive() { return isActive; }

    public void incrementClickCount() { this.clickCount++; }

    public boolean canBeUsed() {
        return isActive && clickCount < maxClicks && LocalDateTime.now().isBefore(expiresAt);
    }

    public String getUnavailabilityReason() {
        if (!isActive) return "Ссылка деактивирована";
        if (clickCount >= maxClicks) return "Лимит переходов исчерпан";
        if (LocalDateTime.now().isAfter(expiresAt)) return "Время жизни истекло";
        return null;
    }
}