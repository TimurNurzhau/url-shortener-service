package com.example.urlshortener.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

class UserRepositoryTest {
    private UserRepository userRepository;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        testUserId = UUID.randomUUID();
    }

    @Test
    void testSaveAndFindByUsername() {
        UserCredentials credentials =
                new UserCredentials("testuser", "hash", testUserId, UserRole.USER);
        userRepository.save(credentials);

        Optional<UserCredentials> found = userRepository.findByUsername("testuser");
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testUsernameExists() {
        UserCredentials credentials =
                new UserCredentials("testuser", "hash", testUserId, UserRole.USER);
        userRepository.save(credentials);

        assertTrue(userRepository.usernameExists("testuser"));
        assertFalse(userRepository.usernameExists("nonexistent"));
    }

    @Test
    void testCreateUserFromCredentials() {
        UserCredentials credentials =
                new UserCredentials("testuser", "hash", testUserId, UserRole.USER);
        User user = userRepository.createUserFromCredentials(credentials);

        assertEquals(testUserId, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals(UserRole.USER, user.getRole());
    }
}
