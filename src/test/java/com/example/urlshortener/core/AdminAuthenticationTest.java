// FILE: src/test/java/com/example/urlshortener/core/AdminAuthenticationTest.java
package com.example.urlshortener.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import java.util.UUID;
import java.io.File;

public class AdminAuthenticationTest {

    @Test
    void shouldAuthenticateWithAdminCredentials() {
        UserRepository userRepository = new UserRepository();

        Optional<UserCredentials> adminCredentials = userRepository.findByUsername("admin");

        if (!adminCredentials.isPresent()) {
            System.out.println("Администратор не найден, создаем нового...");
            createAdminUser(userRepository);
            adminCredentials = userRepository.findByUsername("admin");
        }

        assertTrue(adminCredentials.isPresent(), "Администратор должен существовать в системе");

        UserCredentials credentials = adminCredentials.get();
        boolean isPasswordCorrect = PasswordHasher.verifyPassword("admin123", credentials.getPasswordHash());
        assertTrue(isPasswordCorrect, "Пароль должен быть 'admin123'");

        assertEquals(UserRole.ADMIN, credentials.getRole(), "Роль должна быть ADMIN");
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                credentials.getUserId(),
                "ID должен соответствовать администратору");

        User adminUser = userRepository.createUserFromCredentials(credentials);
        assertEquals("admin", adminUser.getUsername());
        assertEquals(UserRole.ADMIN, adminUser.getRole());
        assertTrue(adminUser.isAdmin());

        System.out.println("✅ Авторизация администратора прошла успешно!");
    }

    private void createAdminUser(UserRepository userRepository) {
        UUID adminId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String adminPasswordHash = PasswordHasher.hashPassword("admin123");
        UserCredentials newAdmin = new UserCredentials("admin", adminPasswordHash, adminId, UserRole.ADMIN);
        userRepository.save(newAdmin);
        System.out.println("🔄 Администратор создан: admin / admin123");
    }
}