package com.urlshortener.main;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final Properties properties = new Properties();

    public AppConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            System.out.println("Использую настройки по умолчанию");
        }
    }

    public int getDefaultTtlHours() {
        return Integer.parseInt(properties.getProperty("app.default.ttl.hours", "24"));
    }

    public int getDefaultMaxClicks() {
        return Integer.parseInt(properties.getProperty("app.default.max.clicks", "10"));
    }

    public String getBaseUrl() {
        return properties.getProperty("app.base.url", "http://localhost:8080");
    }

    public int getShortCodeLength() {
        return Integer.parseInt(properties.getProperty("app.short.code.length", "6"));
    }

    public int getCleanupIntervalMinutes() {
        return Integer.parseInt(properties.getProperty("app.cleanup.interval.minutes", "1"));
    }
}