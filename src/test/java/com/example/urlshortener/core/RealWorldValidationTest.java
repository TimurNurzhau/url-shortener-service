// FILE: src/test/java/com/example/urlshortener/core/RealWorldValidationTest.java
package com.example.urlshortener.core;

import org.junit.jupiter.api.Test;

import java.util.UUID;

public class RealWorldValidationTest {

    @Test
    void testWhatHappensWithInvalidUrlsInService() {
        System.out.println("=== РЕАЛЬНОЕ ПОВЕДЕНИЕ С НЕВАЛИДНЫМИ URL ===");

        UrlShortenerService service = new UrlShortenerService(
                new FileLinkRepository(),
                new SystemSettings()
        );

        UUID testUser = UUID.randomUUID();

        // Тестируем различные сценарии
        String[] testCases = {
                "https://valid-url.com",      // валидный
                "http://valid-http.com",      // валидный
                "invalid-url",               // невалидный
                "",                          // пустой
                "https://",                  // неполный
                "example.com",               // без протокола
                "ftp://invalid.com"          // неподдерживаемый протокол
        };

        for (String url : testCases) {
            try {
                System.out.println("\nТестируем URL: '" + url + "'");
                System.out.println("isValidUrl(): " + UrlValidator.isValidUrl(url));

                try {
                    String normalized = UrlValidator.normalizeUrl(url);
                    System.out.println("normalizeUrl(): " + normalized);
                } catch (Exception e) {
                    System.out.println("normalizeUrl() исключение: " + e.getMessage());
                }

                try {
                    UrlValidator.validateUrl(url);
                    System.out.println("validateUrl(): УСПЕХ");
                } catch (Exception e) {
                    System.out.println("validateUrl() исключение: " + e.getMessage());
                }

                try {
                    String shortCode = service.createShortLink(url, testUser);
                    System.out.println("createShortLink(): УСПЕХ -> " + shortCode);
                } catch (Exception e) {
                    System.out.println("createShortLink() исключение: " + e.getMessage());
                }

            } catch (Exception e) {
                System.out.println("Общее исключение: " + e.getMessage());
            }
        }

        System.out.println("\n✅ Реальное поведение системы проверено");
    }
}