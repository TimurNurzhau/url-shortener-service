package com.example.urlshortener.core;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ShortCodeGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 8;
    private static final int MAX_GENERATION_ATTEMPTS = 100; // Константа вместо магического числа
    private static final Random random = new Random();
    private static final Set<String> usedCodes = ConcurrentHashMap.newKeySet();

    // Метод для инициализации кодов при загрузке из хранилища
    public static void initializeWithExistingCodes(Set<String> existingCodes) {
        usedCodes.clear();
        usedCodes.addAll(existingCodes);
        System.out.println("Загружено использованных кодов: " + usedCodes.size());
    }

    public static String generateShortCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public static String generateUniqueShortCode() {
        String code;
        int attempts = 0;

        do {
            code = generateShortCode();
            attempts++;
            if (attempts > MAX_GENERATION_ATTEMPTS) {
                throw new IllegalStateException("Не удалось сгенерировать уникальный код после " + MAX_GENERATION_ATTEMPTS + " попыток");
            }
        } while (usedCodes.contains(code));

        usedCodes.add(code);
        return code;
    }

    public static void releaseCode(String code) {
        usedCodes.remove(code);
    }

    public static int getUsedCodesCount() {
        return usedCodes.size();
    }

    public static void clearUsedCodes() {
        usedCodes.clear();
    }

    public static Set<String> getUsedCodes() {
        return Set.copyOf(usedCodes); // Возвращаем копию для безопасности
    }
}