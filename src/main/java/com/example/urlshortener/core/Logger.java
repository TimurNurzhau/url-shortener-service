package com.example.urlshortener.core;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Logger {
    private static final String LOG_FILE = "url_shortener.log";
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] %s", timestamp, message);

        // Вывод в консоль
        System.out.println(logMessage);

        // Запись в файл
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(logMessage);
        } catch (IOException e) {
            System.err.println("Ошибка записи в лог: " + e.getMessage());
        }
    }

    public static void logUserAction(UUID userId, String action) {
        log(String.format("USER[%s] %s", userId, action));
    }

    public static void logAdminAction(UUID adminId, String action) {
        log(String.format("ADMIN[%s] %s", adminId, action));
    }

    public static void logError(String error, Exception e) {
        log(String.format("ERROR: %s - %s", error, e.getMessage()));
    }
}
