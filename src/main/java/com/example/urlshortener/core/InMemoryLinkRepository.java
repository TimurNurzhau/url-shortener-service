package com.example.urlshortener.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryLinkRepository {
    private final Map<String, Link> links = new HashMap<>();

    public void save(Link link) {
        links.put(link.getShortCode(), link);
    }

    public Optional<Link> findByShortCode(String shortCode) {
        return Optional.ofNullable(links.get(shortCode));
    }
}
