package com.example.urlshortener.core;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {
    private final Map<String, UserCredentials> usersByUsername = new ConcurrentHashMap<>();
    private final Map<UUID, UserCredentials> usersById = new ConcurrentHashMap<>();
    private static final String USERS_FILE = "users.dat";

    public UserRepository() {
        loadUsersFromFile();
        if (usersByUsername.isEmpty()) {
            createDefaultAdmin();
        }
    }

    private void loadUsersFromFile() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            Map<String, UserCredentials> loadedUsers = (Map<String, UserCredentials>) ois.readObject();
            usersByUsername.putAll(loadedUsers);

            for (UserCredentials creds : loadedUsers.values()) {
                usersById.put(creds.getUserId(), creds);
            }

            System.out.println("Загружено пользователей: " + usersByUsername.size());
        } catch (Exception e) {
            System.err.println("Ошибка загрузки пользователей: " + e.getMessage());
        }
    }

    private void saveUsersToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(usersByUsername);
            System.out.println("Пользователи успешно сохранены: " + usersByUsername.size() + " записей");
        } catch (Exception e) {
            System.err.println("Ошибка сохранения пользователей: " + e.getMessage());
            e.printStackTrace(); // Добавляем детали ошибки
        }
    }

    private void createDefaultAdmin() {
        UUID adminId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String adminPasswordHash = PasswordHasher.hashPassword("admin123");
        UserCredentials admin = new UserCredentials("admin", adminPasswordHash, adminId, UserRole.ADMIN);

        usersByUsername.put("admin", admin);
        usersById.put(adminId, admin);
        saveUsersToFile();

       // System.out.println("Создан администратор по умолчанию: admin / admin123");
    }

    public void save(UserCredentials credentials) {
        usersByUsername.put(credentials.getUsername(), credentials);
        usersById.put(credentials.getUserId(), credentials);
        saveUsersToFile();
    }

    public Optional<UserCredentials> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    public Optional<UserCredentials> findById(UUID id) {
        return Optional.ofNullable(usersById.get(id));
    }

    public boolean usernameExists(String username) {
        return usersByUsername.containsKey(username);
    }

    public User createUserFromCredentials(UserCredentials credentials) {
        return new User(credentials.getUserId(), credentials.getUsername(), credentials.getRole());
    }
}