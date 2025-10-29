package com.example.urlshortener.core;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlValidator {

    private static final int MAX_URL_LENGTH = 2048;

    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        String normalizedUrl = normalizeUrl(url.trim());

        try {
            URI uri = new URI(normalizedUrl);

            // Проверяем наличие хоста
            if (uri.getHost() == null) {
                return false;
            }

            // Проверяем допустимые схемы
            String scheme = uri.getScheme();
            if (scheme == null || (!"http".equals(scheme) && !"https".equals(scheme))) {
                return false;
            }

            // Проверяем длину URL
            if (normalizedUrl.length() > MAX_URL_LENGTH) {
                return false;
            }

            // Проверяем, что хост содержит точку (имеет домен)
            if (!uri.getHost().contains(".")) {
                return false;
            }

            return true;

        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static void validateUrl(String url) {
        if (!isValidUrl(url)) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }

    static String normalizeUrl(String url) {
        if (!url.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
            return "https://" + url;
        }
        return url;
    }
}