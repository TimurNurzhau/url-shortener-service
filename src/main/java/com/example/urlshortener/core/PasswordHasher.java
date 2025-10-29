package com.example.urlshortener.core;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String password, String passwordHash) {
        try {
            return BCrypt.checkpw(password, passwordHash);
        } catch (Exception e) {
            Logger.logError("Ошибка проверки пароля", e);
            return false;
        }
    }
}