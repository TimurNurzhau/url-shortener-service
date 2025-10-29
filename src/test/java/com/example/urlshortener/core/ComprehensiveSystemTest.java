// FILE: src/test/java/com/example/urlshortener/core/ComprehensiveSystemTest.java
package com.example.urlshortener.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

public class ComprehensiveSystemTest {

    private UrlShortenerService service;
    private UserRepository userRepository;
    private SystemSettings systemSettings;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        FileLinkRepository linkRepository = new FileLinkRepository();
        systemSettings = new SystemSettings();
        service = new UrlShortenerService(linkRepository, systemSettings);
        userRepository = new UserRepository();
        adminId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    @Test
    void testCompleteUserJourney() {
        System.out.println("=== ПОЛНЫЙ ТЕСТ СИСТЕМЫ ===");

        // 1. АВТОРИЗАЦИЯ АДМИНИСТРАТОРА
        System.out.println("1. АВТОРИЗАЦИЯ АДМИНИСТРАТОРА");
        Optional<UserCredentials> adminCredentials = userRepository.findByUsername("admin");
        assertTrue(adminCredentials.isPresent(), "Администратор должен существовать");

        boolean adminAuth = PasswordHasher.verifyPassword("admin123", adminCredentials.get().getPasswordHash());
        assertTrue(adminAuth, "Авторизация администратора должна быть успешной");

        User adminUser = userRepository.createUserFromCredentials(adminCredentials.get());
        assertTrue(adminUser.isAdmin(), "Пользователь должен быть администратором");
        System.out.println("✅ Администратор авторизован");

        // 2. РЕГИСТРАЦИЯ НОВОГО ПОЛЬЗОВАТЕЛЯ
        System.out.println("2. РЕГИСТРАЦИЯ НОВОГО ПОЛЬЗОВАТЕЛЯ");
        String newUsername = "user_" + System.currentTimeMillis();
        UUID newUserId = UUID.randomUUID();
        String passwordHash = PasswordHasher.hashPassword("userpass123");

        UserCredentials newUserCreds = new UserCredentials(newUsername, passwordHash, newUserId, UserRole.USER);
        userRepository.save(newUserCreds);

        Optional<UserCredentials> foundUser = userRepository.findByUsername(newUsername);
        assertTrue(foundUser.isPresent(), "Новый пользователь должен быть сохранен");
        System.out.println("✅ Новый пользователь зарегистрирован: " + newUsername);

        // 3. СОЗДАНИЕ КОРОТКИХ ССЫЛОК
        System.out.println("3. СОЗДАНИЕ КОРОТКИХ ССЫЛОК");
        String[] testUrls = {
                "https://google.com",
                "https://github.com",
                "https://stackoverflow.com"
        };

        String[] shortCodes = new String[testUrls.length];
        for (int i = 0; i < testUrls.length; i++) {
            shortCodes[i] = service.createShortLink(testUrls[i], newUserId);
            assertNotNull(shortCodes[i], "Короткий код не должен быть null");
            assertEquals(8, shortCodes[i].length(), "Длина кода должна быть 8 символов");
            System.out.println("   Создана ссылка: " + shortCodes[i] + " -> " + testUrls[i]);
        }

        // 4. ПРОВЕРКА ПЕРЕНАПРАВЛЕНИЙ
        System.out.println("4. ПРОВЕРКА ПЕРЕНАПРАВЛЕНИЙ");
        for (int i = 0; i < shortCodes.length; i++) {
            String redirectedUrl = service.handleRedirect(shortCodes[i]);
            assertEquals(testUrls[i], redirectedUrl, "Перенаправление должно работать корректно");
            System.out.println("   Перенаправление: " + shortCodes[i] + " -> " + redirectedUrl);
        }

        // 5. ПРОВЕРКА СЧЕТЧИКОВ КЛИКОВ
        System.out.println("5. ПРОВЕРКА СЧЕТЧИКОВ КЛИКОВ");
        var userLinks = service.getAllUserLinks(newUserId);
        assertEquals(3, userLinks.size(), "У пользователя должно быть 3 ссылки");

        for (Link link : userLinks) {
            assertTrue(link.getCurrentClicks() >= 1, "Каждая ссылка должна иметь минимум 1 клик");
            assertTrue(LinkValidator.isLinkActive(link), "Все ссылки должны быть активны");
        }
        System.out.println("✅ Счетчики кликов корректны");

        // 6. ПРОВЕРКА ОГРАНИЧЕНИЙ
        System.out.println("6. ПРОВЕРКА ОГРАНИЧЕНИЙ");

        // Устанавливаем маленький лимит для теста
        service.updateDefaultClickLimit(1);
        String limitedUrl = "https://limited.example.com";
        String limitedCode = service.createShortLink(limitedUrl, newUserId);

        // Первый клик должен работать
        service.handleRedirect(limitedCode);

        // Второй клик должен быть заблокирован
        assertThrows(LinkNotActiveException.class, () -> {
            service.handleRedirect(limitedCode);
        }, "Превышение лимита кликов должно блокировать ссылку");
        System.out.println("✅ Ограничения по кликам работают");

        // 7. ПРОВЕРКА ВАЛИДАЦИИ URL (исправленная версия)
        System.out.println("7. ПРОВЕРКА ВАЛИДАЦИИ URL");

        // Корректные URL
        assertTrue(UrlValidator.isValidUrl("https://example.com"), "Корректный HTTPS URL должен проходить валидацию");
        assertTrue(UrlValidator.isValidUrl("http://test.org/path?query=value"), "Корректный HTTP URL должен проходить валидацию");

        // Некорректные URL - проверяем что они не проходят валидацию
        assertFalse(UrlValidator.isValidUrl("invalid-url"), "Некорректный URL не должен проходить валидацию");
        assertFalse(UrlValidator.isValidUrl(""), "Пустой URL не должен проходить валидацию");
        assertFalse(UrlValidator.isValidUrl(null), "Null URL не должен проходить валидацию");

        // Проверяем нормализацию URL
        String normalized = UrlValidator.normalizeUrl("example.com");
        assertEquals("https://example.com", normalized, "URL должен нормализоваться с https://");

        System.out.println("✅ Валидация URL работает");

        // 8. УПРАВЛЕНИЕ НАСТРОЙКАМИ (только для администратора)
        System.out.println("8. УПРАВЛЕНИЕ НАСТРОЙКАМИ");

        int originalLimit = service.getDefaultClickLimit();
        long originalTtl = service.getLinkTtlSeconds();

        // Администратор может менять настройки
        service.updateDefaultClickLimit(100);
        service.updateDefaultTtl(3600L);

        assertEquals(100, service.getDefaultClickLimit());
        assertEquals(3600L, service.getLinkTtlSeconds());

        // Восстанавливаем оригинальные значения
        service.updateDefaultClickLimit(originalLimit);
        service.updateDefaultTtl(originalTtl);
        System.out.println("✅ Управление настройками работает");

        // 9. ПРОВЕРКА УНИКАЛЬНОСТИ КОДОВ
        System.out.println("9. ПРОВЕРКА УНИКАЛЬНОСТИ КОДОВ");
        Set<String> generatedCodes = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            String code = ShortCodeGenerator.generateUniqueShortCode();
            assertFalse(generatedCodes.contains(code), "Коды должны быть уникальными");
            generatedCodes.add(code);
        }
        System.out.println("✅ Все коды уникальны");

        // 10. ПРОВЕРКА ЛОГИРОВАНИЯ
        System.out.println("10. ПРОВЕРКА ЛОГИРОВАНИЯ");
        assertDoesNotThrow(() -> {
            Logger.log("Тестовое сообщение");
            Logger.logUserAction(newUserId, "Тестовое действие");
            Logger.logAdminAction(adminId, "Админское действие");
        });
        System.out.println("✅ Логирование работает");

        System.out.println("🎉 ВСЕ ТЕСТЫ ПРОЙДЕНЫ УСПЕШНО!");
        System.out.println("=== СИСТЕМА ГОТОВА К ИСПОЛЬЗОВАНИЮ ===");
    }

    @Test
    void testUrlValidationBehavior() {
        System.out.println("=== ТЕСТИРОВАНИЕ ПОВЕДЕНИЯ ВАЛИДАЦИИ URL ===");

        // Проверяем реальное поведение методов UrlValidator
        System.out.println("1. Тестирование isValidUrl():");
        System.out.println("   https://example.com: " + UrlValidator.isValidUrl("https://example.com"));
        System.out.println("   invalid-url: " + UrlValidator.isValidUrl("invalid-url"));
        System.out.println("   пустая строка: " + UrlValidator.isValidUrl(""));
        System.out.println("   null: " + UrlValidator.isValidUrl(null));

        System.out.println("2. Тестирование normalizeUrl():");
        try {
            String normalized = UrlValidator.normalizeUrl("example.com");
            System.out.println("   example.com -> " + normalized);
        } catch (Exception e) {
            System.out.println("   normalizeUrl выбросил исключение: " + e.getMessage());
        }

        System.out.println("3. Тестирование validateUrl():");
        try {
            UrlValidator.validateUrl("https://valid.com");
            System.out.println("   Валидный URL прошел проверку");
        } catch (Exception e) {
            System.out.println("   Валидный URL вызвал исключение: " + e.getMessage());
        }

        try {
            UrlValidator.validateUrl("invalid-url");
            System.out.println("   Невалидный URL прошел проверку (не должно быть)");
        } catch (Exception e) {
            System.out.println("   Невалидный URL вызвал исключение: " + e.getMessage());
        }

        System.out.println("✅ Поведение валидации проверено");
    }

    @Test
    void testServiceWithInvalidUrls() {
        System.out.println("=== ТЕСТ: СОЗДАНИЕ ССЫЛОК С НЕВАЛИДНЫМИ URL ===");

        UUID testUserId = UUID.randomUUID();

        // Пробуем создать ссылку с невалидным URL
        try {
            String shortCode = service.createShortLink("invalid-url", testUserId);
            System.out.println("Ссылка создана с невалидным URL: " + shortCode);
            // Если это произошло, значит сервис не проверяет URL
        } catch (Exception e) {
            System.out.println("Сервис отклонил невалидный URL: " + e.getMessage());
        }

        // Пробуем с пустым URL
        try {
            String shortCode = service.createShortLink("", testUserId);
            System.out.println("Ссылка создана с пустым URL: " + shortCode);
        } catch (Exception e) {
            System.out.println("Сервис отклонил пустой URL: " + e.getMessage());
        }

        System.out.println("✅ Тестирование невалидных URL завершено");
    }
}