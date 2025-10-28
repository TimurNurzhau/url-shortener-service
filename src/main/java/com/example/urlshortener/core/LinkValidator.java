package com.example.urlshortener.core;

import java.time.Instant;

public class LinkValidator {
    public static boolean isLinkActive(Link link) {
        if (link == null) {
            return false;
        }

        boolean notExpired = Instant.now().isBefore(link.getExpirationTime());
        boolean withinClickLimit = link.getCurrentClicks() < link.getClickLimit();

        return notExpired && withinClickLimit;
    }
}