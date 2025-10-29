package com.example.urlshortener.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void testHashAndVerifyPassword() {
        String password = "testPassword123";

        String hash = PasswordHasher.hashPassword(password);

        assertNotNull(hash);
        assertNotEquals(password, hash);
        assertTrue(PasswordHasher.verifyPassword(password, hash));
    }

    @Test
    void testVerifyPassword_WrongPassword() {
        String password = "testPassword123";
        String wrongPassword = "wrongPassword";

        String hash = PasswordHasher.hashPassword(password);

        assertFalse(PasswordHasher.verifyPassword(wrongPassword, hash));
    }

    @Test
    void testHashPassword_EmptyPassword() {
        String emptyPassword = "";

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    PasswordHasher.hashPassword(emptyPassword);
                });
    }

    @Test
    void testDifferentHashesForSamePassword() {
        String password = "samePassword";

        String hash1 = PasswordHasher.hashPassword(password);
        String hash2 = PasswordHasher.hashPassword(password);

        assertNotEquals(hash1, hash2); // Разные соли должны давать разные хеши
        assertTrue(PasswordHasher.verifyPassword(password, hash1));
        assertTrue(PasswordHasher.verifyPassword(password, hash2));
    }
}
