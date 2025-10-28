package com.example.urlshortener.core;

import java.time.Instant;
import java.util.UUID;

public class UrlShortenerService {
    private final InMemoryLinkRepository repository;
    private final int defaultClickLimit;
    private final long linkTtlSeconds;

    public UrlShortenerService(InMemoryLinkRepository repository, int defaultClickLimit, long linkTtlSeconds) {
        this.repository = repository;
        this.defaultClickLimit = defaultClickLimit;
        this.linkTtlSeconds = linkTtlSeconds;
    }

    // Пока оставим методы пустыми, заполним их на следующих шагах
    public String createShortLink(String originalUrl, UUID ownerId) {
        // Генерируем короткий код
        String shortCode = ShortCodeGenerator.generateShortCode();

        // Вычисляем время истечения срока жизни ссылки
        Instant expirationTime = Instant.now().plusSeconds(linkTtlSeconds);

        // Создаем объект ссылки
        Link newLink = new Link(
                originalUrl,
                shortCode,
                ownerId,
                defaultClickLimit,
                expirationTime
        );

        // Сохраняем ссылку в репозитории
        repository.save(newLink);

        // Возвращаем короткий код
        return shortCode;
    }

    public String handleRedirect(String shortCode) {
        // Ищем ссылку в репозитории
        Link link = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Ссылка не найдена")); // Пока просто RuntimeException, потом улучшим

        // Проверяем, активна ли ссылка
        if (!LinkValidator.isLinkActive(link)) {
            throw new RuntimeException("Ссылка неактивна"); // Потом тоже улучшим
        }

        // Увеличиваем счетчик кликов
        link.incrementClicks();

        // Возвращаем оригинальный URL для перенаправления
        return link.getOriginalUrl();
    }
}