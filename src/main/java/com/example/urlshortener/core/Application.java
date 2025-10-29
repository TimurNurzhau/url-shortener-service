package com.example.urlshortener.core;

import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Application {
    private final UrlShortenerService service;
    private final Scanner scanner;
    private final UserRepository userRepository;
    private User currentUser;
    private UserCredentials currentCredentials;

    public Application(UrlShortenerService service, UserRepository userRepository) {
        this.service = service;
        this.scanner = new Scanner(System.in);
        this.userRepository = userRepository;
    }

    public void start() {
        showWelcomeScreen();

        if (!authenticateUser()) {
            System.out.println("Выход из приложения.");
            return;
        }

        showMainApplication();
    }

    private void showWelcomeScreen() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                  СЕРВИС СОКРАЩЕНИЯ ССЫЛОК                  ║");
        System.out.println("║         С ограничением переходов и времени жизни           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }

    private boolean authenticateUser() {
        while (true) {
            System.out.println("\n--- СИСТЕМА АУТЕНТИФИКАЦИИ ---");
            System.out.println("1. Вход в систему");
            System.out.println("2. Регистрация нового пользователя");
            System.out.println("3. Выход");
            System.out.print("👉 Выберите действие (1-3): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    if (login()) return true;
                    break;
                case "2":
                    if (register()) return true;
                    break;
                case "3":
                    return false;
                default:
                    System.out.println("❌ Неверный выбор. Попробуйте снова.");
            }
        }
    }

    private boolean login() {
        System.out.println("\n--- ВХОД В СИСТЕМУ ---");
        System.out.print("Логин: ");
        String username = scanner.nextLine().trim();

        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("❌ Логин и пароль не могут быть пустыми.");
            return false;
        }

        Optional<UserCredentials> credentialsOpt = userRepository.findByUsername(username);
        if (credentialsOpt.isPresent()) {
            UserCredentials credentials = credentialsOpt.get();
            if (PasswordHasher.verifyPassword(password, credentials.getPasswordHash())) {
                this.currentCredentials = credentials;
                this.currentUser = userRepository.createUserFromCredentials(credentials);

                System.out.println("✅ Успешный вход! Добро пожаловать, " + username + "!");
                Logger.logUserAction(currentUser.getId(), "Успешный вход в систему");
                return true;
            }
        }

        System.out.println("❌ Неверный логин или пароль.");
        return false;
    }

    private boolean register() {
        System.out.println("\n--- РЕГИСТРАЦИЯ ---");
        System.out.print("Придумайте логин: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("❌ Логин не может быть пустым.");
            return false;
        }

        if (userRepository.usernameExists(username)) {
            System.out.println("❌ Этот логин уже занят.");
            return false;
        }

        System.out.print("Придумайте пароль: ");
        String password = scanner.nextLine().trim();

        if (password.isEmpty()) {
            System.out.println("❌ Пароль не может быть пустым.");
            return false;
        }

        if (password.length() < 4) {
            System.out.println("❌ Пароль должен содержать минимум 4 символа.");
            return false;
        }

        UUID userId = UUID.randomUUID();
        String passwordHash = PasswordHasher.hashPassword(password);
        UserCredentials credentials =
                new UserCredentials(username, passwordHash, userId, UserRole.USER);

        userRepository.save(credentials);
        this.currentCredentials = credentials;
        this.currentUser = userRepository.createUserFromCredentials(credentials);

        System.out.println("✅ Регистрация успешна!");
        System.out.println("🔐 Ваш логин: " + username);
        System.out.println("🆔 Ваш ID: " + userId);
        Logger.logUserAction(userId, "Зарегистрирован новый пользователь: " + username);
        return true;
    }

    private void showMainApplication() {
        printWelcomeMessage();
        checkUserNotifications();

        while (true) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    createShortLink();
                    break;
                case "2":
                    handleRedirect();
                    break;
                case "3":
                    showMyLinks();
                    break;
                case "4":
                    editClickLimit();
                    break;
                case "5":
                    showProfile();
                    break;
                case "6":
                    System.out.println("\nСпасибо за использование нашего сервиса! До свидания!");
                    Logger.logUserAction(currentUser.getId(), "Выход из системы");
                    return;
                default:
                    System.out.println("\nНеверный выбор. Пожалуйста, введите число от 1 до 6.");
            }
        }
    }

    private void printWelcomeMessage() {
        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("Добро пожаловать, " + currentUser.getUsername() + "!");
        System.out.println("Ваша роль: " + currentUser.getRole());
        System.out.println("Ваш ID: " + currentUser.getId());
        System.out.println("═══════════════════════════════════════════════════");
    }

    private void printMainMenu() {
        System.out.println("\n--- ГЛАВНОЕ МЕНЮ ---");
        System.out.println("1. Создать короткую ссылку");
        System.out.println("2. Перейти по короткой ссылке");
        System.out.println("3. Мои ссылки");
        System.out.println("4. Изменить лимит переходов");
        System.out.println("5. Мой профиль");
        System.out.println("6. Выйти");
        System.out.print("👉 Выберите действие (1-6): ");
    }

    private void showProfile() {
        System.out.println("\n--- МОЙ ПРОФИЛЬ ---");
        System.out.println("Логин: " + currentUser.getUsername());
        System.out.println("ID: " + currentUser.getId());
        System.out.println("Роль: " + currentUser.getRole());

        var userLinks = service.getAllUserLinks(currentUser.getId());
        System.out.println("Создано ссылок: " + userLinks.size());

        long activeLinks =
                userLinks.stream().filter(link -> LinkValidator.isLinkActive(link)).count();
        System.out.println("Активных ссылок: " + activeLinks);
    }

    private void createShortLink() {
        System.out.println("\n--- СОЗДАНИЕ КОРОТКОЙ ССЫЛКИ ---");
        System.out.print("Введите длинную ссылку: ");
        String originalUrl = scanner.nextLine().trim();

        // Проверка на пустую строку
        if (originalUrl == null || originalUrl.trim().isEmpty()) {

            System.out.println("❌ Ссылка не может быть пустой.");
            return;
        }

        // Валидация URL
        try {
            UrlValidator.validateUrl(originalUrl);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка валидации URL: " + e.getMessage());
            System.out.println("💡 Пример корректного URL: https://www.example.com");
            return;
        }

        // Создание короткой ссылки
        try {
            String shortCode = service.createShortLink(originalUrl, currentUser.getId());

            System.out.println("✅ Короткая ссылка успешно создана!");
            System.out.println("🔗 Ваша короткая ссылка: " + shortCode);
            System.out.println("📊 Лимит переходов: " + service.getDefaultClickLimit());
            System.out.println("⏰ Время жизни: " + service.getLinkTtlSeconds() + " секунд");

            Logger.logUserAction(
                    currentUser.getId(),
                    "Создана короткая ссылка: " + shortCode + " для URL: " + originalUrl);

        } catch (Exception e) {
            System.out.println("❌ Ошибка при создании ссылки: " + e.getMessage());
            Logger.logError(
                    "Ошибка создания ссылки для пользователя " + currentUser.getUsername(), e);
        }
    }

    private void handleRedirect() {
        System.out.println("\n--- ПЕРЕХОД ПО КОРОТКОЙ ССЫЛКЕ ---");
        System.out.print("Введите короткий код: ");
        String shortCode = scanner.nextLine().trim();

        if (shortCode.isEmpty()) {
            System.out.println("❌ Код не может быть пустым.");
            return;
        }

        try {
            String originalUrl = service.handleRedirect(shortCode);
            System.out.println("🔗 Перенаправление на: " + originalUrl);

            // Улучшенная логика открытия URL
            openUrlInBrowser(originalUrl);

        } catch (LinkNotFoundException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        } catch (LinkNotActiveException e) {
            System.out.println("🚫 Ссылка недоступна: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Неизвестная ошибка: " + e.getMessage());
            Logger.logError("Ошибка при обработке редиректа для кода: " + shortCode, e);
        }
    }

    private void openUrlInBrowser(String url) {
        try {
            // Проверяем, поддерживается ли Desktop API
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    desktop.browse(new java.net.URI(url));
                    System.out.println("✅ Ссылка открыта в браузере!");
                    return;
                }
            }

            // Альтернативные методы для разных ОС
            String os = System.getProperty("os.name").toLowerCase();
            Runtime runtime = Runtime.getRuntime();

            if (os.contains("win")) {
                // Windows
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                // macOS
                runtime.exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux
                runtime.exec("xdg-open " + url);
            } else {
                throw new UnsupportedOperationException("Неподдерживаемая операционная система");
            }

            System.out.println("✅ Команда для открытия браузера выполнена!");

        } catch (Exception e) {
            System.out.println("⚠️ Не удалось автоматически открыть браузер: " + e.getMessage());
            System.out.println("📋 Скопируйте ссылку выше и откройте вручную");
            System.out.println("🔗 " + url);

            // Предлагаем пользователю скопировать ссылку в буфер обмена
            offerClipboardCopy(url);
        }
    }

    private void offerClipboardCopy(String url) {
        try {
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            java.awt.datatransfer.Clipboard clipboard = toolkit.getSystemClipboard();
            java.awt.datatransfer.StringSelection selection =
                    new java.awt.datatransfer.StringSelection(url);
            clipboard.setContents(selection, null);
            System.out.println("📎 Ссылка скопирована в буфер обмена!");
        } catch (Exception e) {
            System.out.println("❌ Не удалось скопировать в буфер обмена");
        }
    }

    private void showMyLinks() {
        System.out.println("\n--- МОИ ССЫЛКИ ---");
        var myLinks = service.getAllUserLinks(currentUser.getId());

        if (myLinks.isEmpty()) {
            System.out.println("📭 У вас пока нет созданных ссылок.");
            System.out.println(
                    "💡 Используйте опцию 'Создать короткую ссылку' чтобы добавить первую ссылку!");
        } else {
            System.out.println("📊 Всего ссылок: " + myLinks.size());
            System.out.println();

            myLinks.forEach(
                    link -> {
                        String status =
                                LinkValidator.isLinkActive(link) ? "🟢 АКТИВНА" : "🔴 НЕАКТИВНА";
                        String details =
                                String.format(
                                        "📈 Переходы: %d/%d, ⏰ Истекает: %s",
                                        link.getCurrentClicks(),
                                        link.getClickLimit(),
                                        link.getExpirationTime().toString().substring(0, 16));

                        System.out.printf(
                                "🔗 Код: %s -> %s [%s]%n",
                                link.getShortCode(), link.getOriginalUrl(), status);
                        System.out.printf("   %s%n", details);
                        System.out.println("   ──────────────────────────────────────────");
                    });
        }
    }

    private void checkUserNotifications() {
        var inactiveLinks = service.getInactiveLinks(currentUser.getId());
        if (!inactiveLinks.isEmpty()) {
            System.out.println("\n--- УВЕДОМЛЕНИЯ ---");
            inactiveLinks.forEach(
                    link -> {
                        String reason;
                        if (link.getCurrentClicks() >= link.getClickLimit()) {
                            reason =
                                    "исчерпан лимит переходов ("
                                            + link.getCurrentClicks()
                                            + "/"
                                            + link.getClickLimit()
                                            + ")";
                        } else {
                            reason = "истек срок действия";
                        }
                        System.out.printf(
                                "🔔 Ссылка %s недоступна: %s%n", link.getShortCode(), reason);
                    });
            System.out.println("────────────────────");
        }
    }

    private void editClickLimit() {
        if (!currentUser.isAdmin()) {
            System.out.println(
                    "❌ Ошибка: только администраторы могут изменять системные настройки");
            System.out.println("💡 Ваша роль: " + currentUser.getRole());
            return;
        }

        System.out.println("\n--- УПРАВЛЕНИЕ СИСТЕМНЫМИ НАСТРОЙКАМИ ---");
        System.out.println("Текущие настройки системы:");
        System.out.println("1. Лимит переходов: " + service.getDefaultClickLimit());
        System.out.println("2. Время жизни ссылок: " + service.getLinkTtlSeconds() + " секунд");

        System.out.print("\nВыберите настройку для изменения (1-2) или 0 для отмены: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                changeClickLimit();
                break;
            case "2":
                changeTtlLimit();
                break;
            case "0":
                System.out.println("Отмена.");
                break;
            default:
                System.out.println("❌ Неверный выбор.");
        }
    }

    private void changeClickLimit() {
        System.out.print("Введите новый системный лимит переходов: ");
        try {
            int newLimit = Integer.parseInt(scanner.nextLine());
            service.updateDefaultClickLimit(newLimit);
            System.out.println("✅ Лимит переходов обновлен!");
            Logger.logAdminAction(
                    currentUser.getId(), "Изменен системный лимит переходов на: " + newLimit);
        } catch (NumberFormatException e) {
            System.out.println("❌ Ошибка: введите корректное число.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void changeTtlLimit() {
        System.out.print("Введите новое время жизни ссылок (в секундах): ");
        try {
            long newTtl = Long.parseLong(scanner.nextLine());
            service.updateDefaultTtl(newTtl);
            System.out.println("✅ Время жизни ссылок обновлено!");
            Logger.logAdminAction(
                    currentUser.getId(), "Изменено время жизни ссылок на: " + newTtl + " секунд");
        } catch (NumberFormatException e) {
            System.out.println("❌ Ошибка: введите корректное число.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            FileLinkRepository linkRepository = new FileLinkRepository();
            UserRepository userRepository = new UserRepository();
            SystemSettings systemSettings = new SystemSettings();

            UrlShortenerService service = new UrlShortenerService(linkRepository, systemSettings);

            ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1);
            cleanupScheduler.scheduleAtFixedRate(
                    () -> {
                        linkRepository.removeExpiredLinks();
                    },
                    0,
                    60,
                    TimeUnit.SECONDS);

            Application app = new Application(service, userRepository);
            app.start();

            cleanupScheduler.shutdown();
        } catch (Exception e) {
            System.err.println("Ошибка запуска приложения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
