package com.urlshortener.main;

import com.urlshortener.model.ShortLink;
import com.urlshortener.service.UrlShortenerService;

import java.awt.*;
import java.net.URI;
import java.util.*;

public class ConsoleMenu {
    private final UrlShortenerService service;
    private final AppConfig config;
    private final Scanner scanner;
    private UUID currentUserId;

    public ConsoleMenu(UrlShortenerService service, AppConfig config) {
        this.service = service;
        this.config = config;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== СЕРВИС КОРОТКИХ ССЫЛОК ===");
        initializeUser();
        showMainMenu();
    }

    private void initializeUser() {
        System.out.println("Введите ваш User ID (или Enter для нового):");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            currentUserId = UUID.randomUUID();
            System.out.println("Новый пользователь: " + currentUserId);
        } else {
            try {
                currentUserId = UUID.fromString(input);
                System.out.println("Восстановлен пользователь: " + currentUserId);
            } catch (Exception e) {
                currentUserId = UUID.randomUUID();
                System.out.println("Создан новый пользователь: " + currentUserId);
            }
        }
    }

    private void showMainMenu() {
        while (true) {
            System.out.println("\n=== ГЛАВНОЕ МЕНЮ ===");
            System.out.println("1. Создать короткую ссылку");
            System.out.println("2. Перейти по короткой ссылке");
            System.out.println("3. Мои ссылки");
            System.out.println("4. Выход");
            System.out.print("Выберите: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    createShortLink();
                    break;
                case "2":
                    processShortLinkClick();
                    break;
                case "3":
                    showUserLinks();
                    break;
                case "4":
                    service.shutdown();
                    System.out.println("Пока!");
                    return;
                default:
                    System.out.println("Неверный выбор!");
                    break;
            }
        }
    }

    private void createShortLink() {
        System.out.print("Введите длинный URL: ");
        String originalUrl = scanner.nextLine().trim();

        try {
            ShortLink link = service.createShortLink(originalUrl, currentUserId);
            System.out.println("✅ Создана: " + config.getBaseUrl() + "/" + link.getShortCode());
            System.out.println("Лимит: " + link.getMaxClicks() + " переходов");
            System.out.println("Время жизни: " + config.getDefaultTtlHours() + " часов");
        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void processShortLinkClick() {
        System.out.print("Введите короткий код или полную ссылку: ");
        String input = scanner.nextLine().trim();

        // Извлекаем короткий код из полной ссылки (если ввели полную)
        String shortCode = input;
        if (input.contains("/")) {
            shortCode = input.substring(input.lastIndexOf('/') + 1);
        }

        try {
            String originalUrl = service.processClick(shortCode);
            System.out.println("🔗 Открываю: " + originalUrl);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(originalUrl));
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void showUserLinks() {
        var userLinks = service.getUserLinks(currentUserId);

        if (userLinks.isEmpty()) {
            System.out.println("У вас нет ссылок");
            return;
        }

        System.out.println("\n=== ВАШИ ССЫЛКИ ===");
        for (ShortLink link : userLinks) {
            System.out.printf("Код: %s | URL: %s | Клики: %d/%d | Активна: %s%n",
                    link.getShortCode(), link.getOriginalUrl(),
                    link.getClickCount(), link.getMaxClicks(),
                    link.canBeUsed() ? "✓" : "✗"
            );
        }
    }
}