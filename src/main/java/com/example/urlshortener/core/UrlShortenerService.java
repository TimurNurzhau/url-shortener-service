package com.example.urlshortener.core;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class UrlShortenerService {
    private final FileLinkRepository repository;
    private final SystemSettings systemSettings;

    public UrlShortenerService(FileLinkRepository repository, SystemSettings systemSettings) {
        this.repository = repository;
        this.systemSettings = systemSettings;
    }

    public String createShortLink(String originalUrl, UUID ownerId) {
        // Явная валидация URL перед созданием ссылки
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL не может быть пустым");
        }

        try {
            UrlValidator.validateUrl(originalUrl);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Некорректный URL: " + e.getMessage());
        }

        String shortCode = ShortCodeGenerator.generateUniqueShortCode(ownerId);
        Instant expirationTime = Instant.now().plusSeconds(systemSettings.getLinkTtlSeconds());

        Link newLink =
                new Link(
                        originalUrl,
                        shortCode,
                        ownerId,
                        systemSettings.getDefaultClickLimit(),
                        expirationTime);

        repository.save(newLink);
        return shortCode;
    }

    public String handleRedirect(String shortCode) {
        Link link =
                repository
                        .findByShortCode(shortCode)
                        .orElseThrow(() -> new LinkNotFoundException("Ссылка не найдена"));

        if (!LinkValidator.isLinkActive(link)) {
            throw new LinkNotActiveException(
                    "Ссылка неактивна (истек срок или превышен лимит переходов)");
        }

        link.incrementClicks();

        repository.save(link);
        return link.getOriginalUrl();
    }

    public List<Link> getInactiveLinks(UUID ownerId) {
        return repository.findByOwnerId(ownerId).stream()
                .filter(link -> !LinkValidator.isLinkActive(link))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Link> getAllUserLinks(UUID ownerId) {
        return repository.findByOwnerId(ownerId);
    }

    public void updateDefaultClickLimit(int newLimit) {
        if (newLimit <= 0) {
            throw new IllegalArgumentException("Лимит переходов должен быть положительным числом");
        }
        systemSettings.setDefaultClickLimit(newLimit);
        System.out.println("Системный лимит переходов изменен на: " + newLimit);
    }

    public void updateDefaultTtl(long newTtlSeconds) {
        if (newTtlSeconds <= 0) {
            throw new IllegalArgumentException("Время жизни должно быть положительным числом");
        }
        systemSettings.setLinkTtlSeconds(newTtlSeconds);
        System.out.println(
                "Системное время жизни ссылок изменено на: " + newTtlSeconds + " секунд");
    }

    public int getDefaultClickLimit() {
        return systemSettings.getDefaultClickLimit();
    }

    public long getLinkTtlSeconds() {
        return systemSettings.getLinkTtlSeconds();
    }
}
