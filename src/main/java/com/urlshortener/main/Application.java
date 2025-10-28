package com.urlshortener.main;

import com.urlshortener.service.UrlShortenerService;

public class Application {
    public static void main(String[] args) {
        System.out.println("Запуск фабрики ссылок...");

        AppConfig config = new AppConfig();
        UrlShortenerService service = new UrlShortenerService(config);
        ConsoleMenu menu = new ConsoleMenu(service, config);

        menu.start();
    }
}