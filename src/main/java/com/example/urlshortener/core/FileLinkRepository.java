package com.example.urlshortener.core;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FileLinkRepository {
    private final Map<String, Link> links = new ConcurrentHashMap<>();
    private static final String LINKS_FILE = "links.dat";

    public FileLinkRepository() {
        loadLinksFromFile();
        initializeShortCodes();
    }

    private void loadLinksFromFile() {
        File file = new File(LINKS_FILE);
        if (!file.exists()) {
            System.out.println("Файл ссылок не найден, будет создан новый.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            List<Link> loadedLinks = (List<Link>) ois.readObject();

            for (Link link : loadedLinks) {
                links.put(link.getShortCode(), link);
            }

            System.out.println("Загружено ссылок: " + links.size());
        } catch (EOFException e) {
            System.err.println("Файл ссылок пуст или поврежден: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Ошибка загрузки классов: " + e.getMessage());
            Logger.logError("Ошибка загрузки классов ссылок", e);
        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода при загрузке ссылок: " + e.getMessage());
            Logger.logError("Ошибка ввода-вывода при загрузке ссылок", e);
        } catch (Exception e) {
            System.err.println("Неизвестная ошибка загрузки ссылок: " + e.getMessage());
            Logger.logError("Неизвестная ошибка загрузки ссылок", e);
        }
    }

    private void initializeShortCodes() {
        Set<String> existingCodes = links.keySet();
        ShortCodeGenerator.initializeWithExistingCodes(existingCodes);
    }

    private void saveLinksToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(LINKS_FILE))) {
            oos.writeObject(new ArrayList<>(links.values()));
            System.out.println("Ссылки успешно сохранены: " + links.size() + " записей");
        } catch (IOException e) {
            System.err.println("Критическая ошибка сохранения ссылок: " + e.getMessage());
            Logger.logError("Критическая ошибка сохранения ссылок", e);
            throw new RuntimeException("Не удалось сохранить данные ссылок", e);
        }
    }

    public void save(Link link) {
        if (link == null) {
            throw new IllegalArgumentException("Ссылка не может быть null");
        }
        links.put(link.getShortCode(), link);
        saveLinksToFile();
    }

    public Optional<Link> findByShortCode(String shortCode) {
        if (shortCode == null || shortCode.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(links.get(shortCode));
    }

    public List<Link> findByOwnerId(UUID ownerId) {
        if (ownerId == null) {
            return Collections.emptyList();
        }
        return links.values().stream()
                .filter(link -> ownerId.equals(link.getOwnerId()))
                .collect(Collectors.toList());
    }

    public Set<String> getAllShortCodes() {
        return Set.copyOf(links.keySet());
    }

    public void removeExpiredLinks() {
        Instant now = Instant.now();
        boolean removed = links.entrySet().removeIf(entry -> {
            Link link = entry.getValue();
            if (link == null) {
                return true; // Удаляем null ссылки
            }
            boolean expired = now.isAfter(link.getExpirationTime());
            if (expired) {
                // Освобождаем код для конкретного пользователя
                ShortCodeGenerator.releaseCodeForUser(link.getOwnerId(), entry.getKey());
                Logger.log("Автоматически удалена просроченная ссылка: " + entry.getKey());
            }
            return expired;
        });if (removed) {
            saveLinksToFile();
        }
    }
}