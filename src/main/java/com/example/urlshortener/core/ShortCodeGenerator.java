package com.example.urlshortener.core;

import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShortCodeGenerator {
    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 8;
    private static final int MAX_GENERATION_ATTEMPTS = 1000; // Увеличим количество попыток
    private static final Random random = new Random();

    // Храним использованные коды с привязкой к пользователю: userId -> Set<shortCodes>
    private static final ConcurrentHashMap<UUID, Set<String>> userUsedCodes =
            new ConcurrentHashMap<>();

    // Также храним глобально использованные коды для быстрой проверки уникальности
    private static final Set<String> globalUsedCodes = ConcurrentHashMap.newKeySet();

    public static void initializeWithExistingCodes(Set<String> existingCodes) {
        globalUsedCodes.clear();
        globalUsedCodes.addAll(existingCodes);
        userUsedCodes.clear(); // При инициализации очищаем пользовательские коды
        System.out.println("Загружено использованных кодов: " + globalUsedCodes.size());
    }

    public static String generateShortCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    // Генерация уникального кода для конкретного пользователя
    public static String generateUniqueShortCode(UUID userId) {
        String code;
        int attempts = 0;

        do {
            code = generateShortCode();
            attempts++;
            if (attempts > MAX_GENERATION_ATTEMPTS) {
                throw new IllegalStateException(
                        "Не удалось сгенерировать уникальный код после "
                                + MAX_GENERATION_ATTEMPTS
                                + " попыток");
            }
        } while (isCodeUsed(userId, code));

        // Регистрируем код для пользователя и глобально
        registerCode(userId, code);
        return code;
    }

    // Проверка, используется ли код (учитываем пользователя)
    private static boolean isCodeUsed(UUID userId, String code) {
        // Проверяем глобальную уникальность
        if (globalUsedCodes.contains(code)) {
            return true;
        }

        // Проверяем, не использует ли этот пользователь уже этот код
        Set<String> userCodes = userUsedCodes.get(userId);
        return userCodes != null && userCodes.contains(code);
    }

    // Регистрация кода для пользователя
    private static void registerCode(UUID userId, String code) {
        // Добавляем в глобальный набор
        globalUsedCodes.add(code);

        // Добавляем в набор пользователя
        userUsedCodes.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(code);
    }

    public static void releaseCode(String code) {
        globalUsedCodes.remove(code);

        // Удаляем код из всех пользовательских наборов
        userUsedCodes.values().forEach(userCodes -> userCodes.remove(code));
    }

    // Освобождение кода для конкретного пользователя
    public static void releaseCodeForUser(UUID userId, String code) {
        globalUsedCodes.remove(code);
        Set<String> userCodes = userUsedCodes.get(userId);
        if (userCodes != null) {
            userCodes.remove(code);
        }
    }

    public static int getGlobalUsedCodesCount() {
        return globalUsedCodes.size();
    }

    public static int getUserUsedCodesCount(UUID userId) {
        Set<String> userCodes = userUsedCodes.get(userId);
        return userCodes != null ? userCodes.size() : 0;
    }

    public static void clearUserCodes(UUID userId) {
        Set<String> userCodes = userUsedCodes.remove(userId);
        if (userCodes != null) {
            globalUsedCodes.removeAll(userCodes);
        }
    }

    public static void clearAllCodes() {
        globalUsedCodes.clear();
        userUsedCodes.clear();
    }

    public static Set<String> getGlobalUsedCodes() {
        return Set.copyOf(globalUsedCodes);
    }

    public static Set<String> getUserUsedCodes(UUID userId) {
        Set<String> userCodes = userUsedCodes.get(userId);
        return userCodes != null ? Set.copyOf(userCodes) : Set.of();
    }
}
