package com.example.urlshortener.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class UrlShortenerIntegrationTest {

    private FileLinkRepository linkRepository;
    private SystemSettings systemSettings;
    private UrlShortenerService service;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        linkRepository = new FileLinkRepository();
        systemSettings = new SystemSettings();
        service = new UrlShortenerService(linkRepository, systemSettings);
        testUserId = UUID.randomUUID();

        // Очистка перед тестом
        linkRepository.removeExpiredLinks();
    }

    @Test
    void testCompleteWorkflow() {
        // Создание короткой ссылки
        String originalUrl = "https://www.example.com";
        String shortCode = service.createShortLink(originalUrl, testUserId);

        assertNotNull(shortCode);
        assertEquals(8, shortCode.length());

        // Получение всех ссылок пользователя
        var userLinks = service.getAllUserLinks(testUserId);
        assertEquals(1, userLinks.size());

        // Проверка редиректа
        String redirectedUrl = service.handleRedirect(shortCode);
        assertEquals(originalUrl, redirectedUrl);

        // Проверка увеличения счетчика кликов
        var linkAfterClick = userLinks.get(0);
        assertEquals(1, linkAfterClick.getCurrentClicks());
    }
}
