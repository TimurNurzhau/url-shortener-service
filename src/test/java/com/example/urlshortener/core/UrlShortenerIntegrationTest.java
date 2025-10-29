// FILE: src/test/java/com/example/urlshortener/core/UrlShortenerIntegrationTest.java
package com.example.urlshortener.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

public class UrlShortenerIntegrationTest {

    private UrlShortenerService service;
    private UserRepository userRepository;
    private SystemSettings systemSettings;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        FileLinkRepository linkRepository = new FileLinkRepository();
        systemSettings = new SystemSettings();
        service = new UrlShortenerService(linkRepository, systemSettings);
        userRepository = new UserRepository();
        testUserId = UUID.randomUUID();
    }

    @Test
    void shouldCreateAndRedirectShortLink() {
        System.out.println("=== ТЕСТ: СОЗДАНИЕ И ПЕРЕНАПРАВЛЕНИЕ ССЫЛКИ ===");

        String originalUrl = "https://www.google.com";
        String shortCode = service.createShortLink(originalUrl, testUserId);

        assertNotNull(shortCode, "Короткий код не должен быть null");
        assertEquals(8, shortCode.length(), "Длина кода должна быть 8 символов");
        System.out.println("Создана короткая ссылка: " + shortCode + " -> " + originalUrl);

        String redirectedUrl = service.handleRedirect(shortCode);
        assertEquals(originalUrl, redirectedUrl, "URL перенаправления должен совпадать с оригиналом");
        System.out.println("Перенаправление работает: " + shortCode + " -> " + redirectedUrl);

        Link link = service.getAllUserLinks(testUserId).get(0);
        assertEquals(1, link.getCurrentClicks(), "Счетчик кликов должен увеличиться до 1");
        System.out.println("Счетчик кликов: " + link.getCurrentClicks());
    }

    @Test
    void shouldHandleMultipleUsersAndLinks() {
        System.out.println("=== ТЕСТ: МНОГО ПОЛЬЗОВАТЕЛЕЙ И ССЫЛОК ===");

        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        String url1 = "https://site1.com";
        String url2 = "https://site2.com";
        String url3 = "https://site3.com";

        String code1 = service.createShortLink(url1, user1);
        String code2 = service.createShortLink(url2, user1);
        String code3 = service.createShortLink(url3, user2);

        System.out.println("Пользователь 1 создал: " + code1 + ", " + code2);
        System.out.println("Пользователь 2 создал: " + code3);

        var user1Links = service.getAllUserLinks(user1);
        var user2Links = service.getAllUserLinks(user2);

        assertEquals(2, user1Links.size(), "Пользователь 1 должен иметь 2 ссылки");
        assertEquals(1, user2Links.size(), "Пользователь 2 должен иметь 1 ссылку");

        assertNotEquals(code1, code2, "Коды должны быть уникальными");
        assertNotEquals(code1, code3, "Коды должны быть уникальными");
    }

    @Test
    void shouldRespectClickLimit() {
        System.out.println("=== ТЕСТ: ОГРАНИЧЕНИЕ КЛИКОВ ===");

        systemSettings.setDefaultClickLimit(2);
        String originalUrl = "https://example.com";
        String shortCode = service.createShortLink(originalUrl, testUserId);

        System.out.println("Лимит кликов: 2");
        System.out.println("Короткая ссылка: " + shortCode);

        service.handleRedirect(shortCode);
        System.out.println("Клик 1: успешно");

        service.handleRedirect(shortCode);
        System.out.println("Клик 2: успешно");

        assertThrows(LinkNotActiveException.class, () -> {
            service.handleRedirect(shortCode);
        }, "Третий клик должен вызвать исключение");
        System.out.println("Клик 3: заблокирован (ожидаемо)");

        Link link = service.getAllUserLinks(testUserId).get(0);
        assertEquals(2, link.getCurrentClicks(), "Счетчик должен показывать 2 клика");
        assertFalse(LinkValidator.isLinkActive(link), "Ссылка должна быть неактивной");
        System.out.println("Ссылка неактивна, кликов: " + link.getCurrentClicks() + "/" + link.getClickLimit());
    }

    @Test
    void shouldHandleExpiredLinks() {
        System.out.println("=== ТЕСТ: ПРОСРОЧЕННЫЕ ССЫЛКИ ===");

        systemSettings.setLinkTtlSeconds(1);
        String originalUrl = "https://temporary.com";
        String shortCode = service.createShortLink(originalUrl, testUserId);

        System.out.println("Ссылка создана с TTL 1 секунда: " + shortCode);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThrows(LinkNotActiveException.class, () -> {
            service.handleRedirect(shortCode);
        }, "Просроченная ссылка должна вызывать исключение");
        System.out.println("Ссылка просрочена (ожидаемо)");
    }

    @Test
    void shouldValidateUrls() {
        System.out.println("=== ТЕСТ: ВАЛИДАЦИЯ URL ===");

        assertTrue(UrlValidator.isValidUrl("https://example.com"), "Корректный HTTPS URL");
        assertTrue(UrlValidator.isValidUrl("http://google.com"), "Корректный HTTP URL");
        assertFalse(UrlValidator.isValidUrl("invalid-url"), "Некорректный URL");
        assertFalse(UrlValidator.isValidUrl(""), "Пустой URL");
        assertFalse(UrlValidator.isValidUrl(null), "Null URL");

        System.out.println("✅ Валидация URL работает корректно");
    }

    @Test
    void shouldGenerateUniqueCodes() {
        System.out.println("=== ТЕСТ: ГЕНЕРАЦИЯ УНИКАЛЬНЫХ КОДОВ ===");

        String code1 = ShortCodeGenerator.generateUniqueShortCode();
        String code2 = ShortCodeGenerator.generateUniqueShortCode();
        String code3 = ShortCodeGenerator.generateUniqueShortCode();

        assertNotNull(code1);
        assertNotNull(code2);
        assertNotNull(code3);
        assertEquals(8, code1.length());
        assertEquals(8, code2.length());
        assertEquals(8, code3.length());

        assertNotEquals(code1, code2, "Коды должны быть разными");
        assertNotEquals(code1, code3, "Коды должны быть разными");
        assertNotEquals(code2, code3, "Коды должны быть разными");

        System.out.println("Сгенерированы уникальные коды:");
        System.out.println("Код 1: " + code1);
        System.out.println("Код 2: " + code2);
        System.out.println("Код 3: " + code3);
    }

    @Test
    void shouldHandleUserRegistrationAndLogin() {
        System.out.println("=== ТЕСТ: РЕГИСТРАЦИЯ И АВТОРИЗАЦИЯ ===");

        String username = "testuser_" + System.currentTimeMillis();
        String password = "testpass123";

        System.out.println("Регистрируем пользователя: " + username);

        UUID userId = UUID.randomUUID();
        String passwordHash = PasswordHasher.hashPassword(password);
        UserCredentials credentials = new UserCredentials(username, passwordHash, userId, UserRole.USER);

        userRepository.save(credentials);

        Optional<UserCredentials> found = userRepository.findByUsername(username);
        assertTrue(found.isPresent(), "Пользователь должен быть найден");
        assertEquals(username, found.get().getUsername());
        assertEquals(userId, found.get().getUserId());

        boolean loginSuccess = PasswordHasher.verifyPassword(password, found.get().getPasswordHash());
        assertTrue(loginSuccess, "Авторизация должна быть успешной");

        User user = userRepository.createUserFromCredentials(found.get());
        assertEquals(username, user.getUsername());
        assertEquals(UserRole.USER, user.getRole());
        assertFalse(user.isAdmin(), "Обычный пользователь не должен быть администратором");

        System.out.println("✅ Пользователь успешно зарегистрирован и авторизован");
    }

    @Test
    void shouldManageSystemSettingsAsAdmin() {
        System.out.println("=== ТЕСТ: УПРАВЛЕНИЕ СИСТЕМНЫМИ НАСТРОЙКАМИ ===");

        int originalClickLimit = systemSettings.getDefaultClickLimit();
        long originalTtl = systemSettings.getLinkTtlSeconds();

        System.out.println("Текущие настройки - Лимит кликов: " + originalClickLimit + ", TTL: " + originalTtl + " сек");

        service.updateDefaultClickLimit(50);
        service.updateDefaultTtl(7200L);

        assertEquals(50, service.getDefaultClickLimit(), "Лимит кликов должен обновиться");
        assertEquals(7200L, service.getLinkTtlSeconds(), "TTL должен обновиться");

        System.out.println("Новые настройки - Лимит кликов: " + service.getDefaultClickLimit() + ", TTL: " + service.getLinkTtlSeconds() + " сек");

        assertThrows(IllegalArgumentException.class, () -> {
            service.updateDefaultClickLimit(0);
        }, "Лимит кликов должен быть положительным");

        assertThrows(IllegalArgumentException.class, () -> {
            service.updateDefaultTtl(-1);
        }, "TTL должен быть положительным");

        System.out.println("✅ Управление настройками работает корректно");

        service.updateDefaultClickLimit(originalClickLimit);
        service.updateDefaultTtl(originalTtl);
    }

    @Test
    void shouldLogUserActions() {
        System.out.println("=== ТЕСТ: СИСТЕМА ЛОГИРОВАНИЯ ===");

        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        assertDoesNotThrow(() -> {
            Logger.log("Тестовое сообщение в лог");
            Logger.logUserAction(userId, "Тестовое действие пользователя");
            Logger.logAdminAction(adminId, "Тестовое действие администратора");
            Logger.logError("Тестовая ошибка", new Exception("Тестовое исключение"));
        }, "Логирование не должно вызывать исключений");

        System.out.println("✅ Система логирования работает корректно");
    }

    @Test
    void shouldHandleEdgeCases() {
        System.out.println("=== ТЕСТ: ГРАНИЧНЫЕ СЛУЧАИ ===");

        assertThrows(LinkNotFoundException.class, () -> {
            service.handleRedirect("nonexistent");
        }, "Несуществующая ссылка должна вызывать исключение");


        System.out.println("✅ Граничные случаи обрабатываются корректно");
    }
}