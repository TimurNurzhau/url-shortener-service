package com.example.urlshortener.core;

import java.time.Instant;
import java.util.*;

public class InMemoryLinkRepository {
    private final Map<String, Link> links = new HashMap<>();

    public void save(Link link) {
        links.put(link.getShortCode(), link);
    }

    public Optional<Link> findByShortCode(String shortCode) {
        return Optional.ofNullable(links.get(shortCode));
    }

    public List<Link> findByOwnerId(UUID ownerId) {
        return links.values().stream()
                .filter(link -> link.getOwnerId().equals(ownerId))
                .collect(java.util.stream.Collectors.toList());
    }

    public void removeExpiredLinks() {
        Instant now = Instant.now();
        links.entrySet()
                .removeIf(
                        entry -> {
                            boolean expired = now.isAfter(entry.getValue().getExpirationTime());
                            if (expired) {
                                // Освобождаем код при удалении ссылки
                                ShortCodeGenerator.releaseCode(entry.getKey());
                            }
                            return expired;
                        });
    }
}
