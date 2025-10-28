package com.example.urlshortener.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private final Properties properties;

    public Config() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Не удалось найти файл config.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки конфигурации", e);
        }
    }

    public int getDefaultClickLimit() {
        return Integer.parseInt(properties.getProperty("default.click.limit"));
    }

    public long getLinkTtlSeconds() {
        return Long.parseLong(properties.getProperty("link.ttl.seconds"));
    }
}