package com.urlshortener.service;

import com.urlshortener.main.AppConfig;
import com.urlshortener.model.ShortLink;
import com.urlshortener.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class UrlShortenerService {
    private final AppConfig config;
    private final Map<String, ShortLink> linksByShortCode = new HashMap<>();
    private final Map<String, ShortLink> linksById = new HashMap<>();
    private final Map<UUID, User> usersById = new HashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
    private final Random random = new Random();
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public UrlShortenerService(AppConfig config) {
        this.config = config;
        startCleanupTask();
    }

    public ShortLink createShortLink(String originalUrl, UUID userId) {
        User user = usersById.computeIfAbsent(userId, User::new);
        String shortCode = generateUniqueShortCode();

        ShortLink link = new ShortLink(originalUrl, shortCode, userId,
                config.getDefaultMaxClicks(), config.getDefaultTtlHours());

        linksByShortCode.put(shortCode, link);
        linksById.put(link.getId(), link);
        user.addCreatedLink(link.getId());

        return link;
    }

    private String generateUniqueShortCode() {
        String shortCode;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < config.getShortCodeLength(); i++) {
                sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
            shortCode = sb.toString();
        } while (linksByShortCode.containsKey(shortCode));
        return shortCode;
    }

    public String processClick(String shortCode) {
        ShortLink link = linksByShortCode.get(shortCode);
        if (link == null) throw new IllegalArgumentException("Ссылка не найдена");
        if (!link.canBeUsed()) throw new IllegalStateException(link.getUnavailabilityReason());

        link.incrementClickCount();
        return link.getOriginalUrl();
    }

    public List<ShortLink> getUserLinks(UUID userId) {
        User user = usersById.get(userId);
        if (user == null) return Collections.emptyList();

        // ИСПРАВЛЕННАЯ СТРОКА - используем Collectors.toList() вместо .toList()
        return user.getCreatedLinkIds().stream()
                .map(linksById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean deleteLink(UUID userId, String linkId) {
        User user = usersById.get(userId);
        if (user == null || !user.ownsLink(linkId)) return false;

        ShortLink link = linksById.get(linkId);
        if (link != null) {
            linksByShortCode.remove(link.getShortCode());
            linksById.remove(linkId);
            user.removeLink(linkId);
            return true;
        }
        return false;
    }

    private void startCleanupTask() {
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredLinks,
                config.getCleanupIntervalMinutes(), config.getCleanupIntervalMinutes(), TimeUnit.MINUTES);
    }

    private void cleanupExpiredLinks() {
        LocalDateTime now = LocalDateTime.now();
        linksByShortCode.values().removeIf(link -> now.isAfter(link.getExpiresAt()));
    }

    public void shutdown() {
        cleanupScheduler.shutdown();
    }
}