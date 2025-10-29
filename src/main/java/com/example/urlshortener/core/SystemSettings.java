package com.example.urlshortener.core;

import java.io.*;
import java.util.Properties;

public class SystemSettings {
    private static final String SETTINGS_FILE = "system_settings.properties";
    private final Properties properties;

    public SystemSettings() {
        properties = new Properties();
        loadSettings();
    }

    private void loadSettings() {
        try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
            properties.load(input);
        } catch (FileNotFoundException e) {
            // Файл не существует - используем значения по умолчанию
            setDefaultValues();
            saveSettings();
        } catch (IOException e) {
            System.err.println("Ошибка загрузки настроек: " + e.getMessage());
            setDefaultValues();
        }
    }

    private void setDefaultValues() {
        properties.setProperty("default.click.limit", "10");
        properties.setProperty("link.ttl.seconds", "600");
    }

    private void saveSettings() {
        try (OutputStream output = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(output, "System Settings for URL Shortener");
        } catch (IOException e) {
            System.err.println("Ошибка сохранения настроек: " + e.getMessage());
        }
    }

    // Геттеры
    public int getDefaultClickLimit() {
        return Integer.parseInt(properties.getProperty("default.click.limit"));
    }

    public long getLinkTtlSeconds() {
        return Long.parseLong(properties.getProperty("link.ttl.seconds"));
    }

    // Сеттеры
    public void setDefaultClickLimit(int limit) {
        properties.setProperty("default.click.limit", String.valueOf(limit));
        saveSettings();
    }

    public void setLinkTtlSeconds(long seconds) {
        properties.setProperty("link.ttl.seconds", String.valueOf(seconds));
        saveSettings();
    }
}