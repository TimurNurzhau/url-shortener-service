package com.example.urlshortener.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class UrlShortenerServiceTest {
    private UrlShortenerService service;
    private FileLinkRepository linkRepository;
    private SystemSettings systemSettings;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        linkRepository = new FileLinkRepository();
        systemSettings = new SystemSettings();
        service = new UrlShortenerService(linkRepository, systemSettings);
        testUserId = UUID.randomUUID();

        // Очищаем репозиторий перед каждым тестом
        linkRepository.removeExpiredLinks();
    }

    @Test
    void testCreateShortLink_ValidUrl() {
        String originalUrl = "https://www.example.com";
        String shortCode = service.createShortLink(originalUrl, testUserId);

        assertNotNull(shortCode);
        assertEquals(8, shortCode.length());
    }

    @Test
    void testCreateShortLink_InvalidUrl() {
        String invalidUrl = "not-a-valid-url";

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    service.createShortLink(invalidUrl, testUserId);
                });
    }

    @Test
    void testHandleRedirect_LinkNotFound() {
        assertThrows(
                LinkNotFoundException.class,
                () -> {
                    service.handleRedirect("nonexistent");
                });
    }

    @Test
    void testGetAllUserLinks() {
        String url = "https://www.example.com";
        service.createShortLink(url, testUserId);

        var userLinks = service.getAllUserLinks(testUserId);
        assertEquals(1, userLinks.size());
        assertEquals(url, userLinks.get(0).getOriginalUrl());
    }
}
