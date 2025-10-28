package com.example.urlshortener.core;

import java.util.Scanner;

public class Application {
    private final UrlShortenerService service;
    private final Scanner scanner;

    public Application(UrlShortenerService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Добро пожаловать в сервис сокращения ссылок!");

        // TODO: здесь будет основной цикл программы

        scanner.close();
    }

    public static void main(String[] args) {
        try {
            // Загружаем конфигурацию
            Config config = new Config();

            // Создаем репозиторий и сервис
            InMemoryLinkRepository repository = new InMemoryLinkRepository();
            UrlShortenerService service = new UrlShortenerService(
                    repository,
                    config.getDefaultClickLimit(),
                    config.getLinkTtlSeconds()
            );

            // Создаем и запускаем приложение
            Application app = new Application(service);
            app.start();

        } catch (Exception e) {
            System.err.println("Ошибка запуска приложения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}