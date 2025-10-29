package com.example.urlshortener.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * Сервис для хеширования и проверки паролей
 */
public class PasswordHasher {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LENGTH = 16;
    private static final int HASH_ITERATIONS = 10000;
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Хеширует пароль с использованием соли и многократного хеширования
     *
     * @param password пароль для хеширования
     * @return хешированная строка в формате Base64 (соль + хеш)
     * @throws IllegalArgumentException если пароль null или пустой
     */
    public static String hashPassword(String password) {
        validatePassword(password);

        try {
            // Генерируем уникальную соль
            byte[] salt = generateSalt();

            // Хешируем пароль с солью и многократными итерациями
            byte[] hashedPassword = hashWithMultipleIterations(password, salt, HASH_ITERATIONS);

            // Комбинируем соль и хеш для хранения
            byte[] combined = combineSaltAndHash(salt, hashedPassword);

            return Base64.getEncoder().encodeToString(combined);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm '" + HASH_ALGORITHM + "' not available", e);
        }
    }

    /**
     * Проверяет соответствие пароля хешу
     *
     * @param rawPassword исходный пароль
     * @param hashedPassword хешированный пароль
     * @return true если пароль соответствует хешу
     * @throws IllegalArgumentException если пароли null или пустые
     */
    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        validateRawPassword(rawPassword);
        validateHashedPassword(hashedPassword);

        try {
            // Декодируем комбинированную строку
            byte[] combined = Base64.getDecoder().decode(hashedPassword);

            // Извлекаем соль и хеш
            byte[] salt = extractSalt(combined);
            byte[] storedHash = extractHash(combined, salt.length);

            // Хешируем введенный пароль с той же солью и итерациями
            byte[] testHash = hashWithMultipleIterations(rawPassword, salt, HASH_ITERATIONS);

            // Сравниваем хеши безопасным способом (constant-time comparison)
            return constantTimeEquals(storedHash, testHash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm '" + HASH_ALGORITHM + "' not available", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid hashed password format", e);
        }
    }

    /**
     * Генерирует случайную соль
     */
    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Хеширует пароль с солью и многократными итерациями
     */
    private static byte[] hashWithMultipleIterations(String password, byte[] salt, int iterations)
            throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

        // Первое хеширование: соль + пароль
        digest.update(salt);
        byte[] hash = digest.digest(password.getBytes());

        // Дополнительные итерации для увеличения сложности
        for (int i = 1; i < iterations; i++) {
            digest.reset();
            hash = digest.digest(hash);
        }

        return hash;
    }

    /**
     * Комбинирует соль и хеш в один массив байтов
     */
    private static byte[] combineSaltAndHash(byte[] salt, byte[] hash) {
        byte[] combined = new byte[salt.length + hash.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(hash, 0, combined, salt.length, hash.length);
        return combined;
    }

    /**
     * Извлекает соль из комбинированного массива
     */
    private static byte[] extractSalt(byte[] combined) {
        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
        return salt;
    }

    /**
     * Извлекает хеш из комбинированного массива
     */
    private static byte[] extractHash(byte[] combined, int saltLength) {
        byte[] hash = new byte[combined.length - saltLength];
        System.arraycopy(combined, saltLength, hash, 0, hash.length);
        return hash;
    }

    /**
     * Сравнивает два массива байтов за постоянное время (защита от timing attacks)
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    /**
     * Валидация исходного пароля
     */
    private static void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty or whitespace");
        }
        if (password.length() > 1000) {
            throw new IllegalArgumentException("Password is too long");
        }
    }

    /**
     * Валидация исходного пароля для проверки
     */
    private static void validateRawPassword(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Raw password cannot be null");
        }
        if (rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Raw password cannot be empty or whitespace");
        }
    }

    /**
     * Валидация хешированного пароля
     */
    private static void validateHashedPassword(String hashedPassword) {
        if (hashedPassword == null) {
            throw new IllegalArgumentException("Hashed password cannot be null");
        }
        if (hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be empty or whitespace");
        }

        // Проверяем, что строка является валидным Base64
        try {
            byte[] decoded = Base64.getDecoder().decode(hashedPassword);
            if (decoded.length <= SALT_LENGTH) {
                throw new IllegalArgumentException("Invalid hashed password format");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Hashed password is not valid Base64", e);
        }
    }
}