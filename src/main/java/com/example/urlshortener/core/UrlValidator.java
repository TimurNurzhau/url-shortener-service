package com.example.urlshortener.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;



public class UrlValidator {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[a-zA-Z0-9+&@#/%?=~_|!:,.;-]*[a-zA-Z0-9+&@#/%=~_|.-]"
    );

    private static final int MAX_URL_LENGTH = 2048;

    public static void validateUrl(String url) {
        System.out.println("DEBUG: validateUrl called with: '" + url + "'");

        if (url == null || url.trim().isEmpty()) {
            System.out.println("DEBUG: Throwing IllegalArgumentException for empty URL");
            throw new IllegalArgumentException("URL не может быть пустым");
        }

        // Проверка длины
        if (url.length() > MAX_URL_LENGTH) {
            throw new IllegalArgumentException("URL слишком длинный (максимум " + MAX_URL_LENGTH + " символов)");
        }

        String normalized = normalizeUrl(url);

        // Финальная проверка валидности URL
        if (!isValidUrl(normalized)) {
            throw new IllegalArgumentException("Некорректный URL: " + url);
        }
    }

    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        // Проверка длины
        if (url.length() > MAX_URL_LENGTH) {
            return false;
        }

        try {
            new URL(url);
            return URL_PATTERN.matcher(url).matches();
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static String normalizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL не может быть пустым");
        }

        String normalized = url.trim();

        // Удаление лишних пробелов (включая внутренние)
        normalized = normalized.replaceAll("\\s+", "");

        // Добавление протокола если отсутствует
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }

        return normalized;
    }
}