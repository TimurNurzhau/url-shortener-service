package com.example.urlshortener.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

class ShortCodeGeneratorTest {

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        ShortCodeGenerator.clearAllCodes();
        testUserId = UUID.randomUUID();
    }

    @Test
    void testGenerateShortCode_ReturnsCorrectLength() {
        String code = ShortCodeGenerator.generateShortCode();
        assertEquals(8, code.length());
    }

    @Test
    void testGenerateUniqueShortCode_ReturnsUniqueCodes() {
        String code1 = ShortCodeGenerator.generateUniqueShortCode(testUserId);
        String code2 = ShortCodeGenerator.generateUniqueShortCode(testUserId);

        assertNotEquals(code1, code2);
        assertEquals(8, code1.length());
        assertEquals(8, code2.length());
    }

    @Test
    void testGenerateUniqueShortCode_WithDifferentUsers() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        String code1 = ShortCodeGenerator.generateUniqueShortCode(user1);
        String code2 = ShortCodeGenerator.generateUniqueShortCode(user2);

        assertNotEquals(code1, code2);
    }

    @Test
    void testInitializeWithExistingCodes() {
        Set<String> existingCodes = Set.of("existing1", "existing2");
        ShortCodeGenerator.initializeWithExistingCodes(existingCodes);

        String newCode = ShortCodeGenerator.generateUniqueShortCode(testUserId);
        assertNotEquals("existing1", newCode);
        assertNotEquals("existing2", newCode);
    }

    @Test
    void testReleaseCode() {
        String code = ShortCodeGenerator.generateUniqueShortCode(testUserId);
        assertTrue(ShortCodeGenerator.getGlobalUsedCodes().contains(code));

        ShortCodeGenerator.releaseCode(code);
        assertFalse(ShortCodeGenerator.getGlobalUsedCodes().contains(code));
    }

    @Test
    void testReleaseCodeForUser() {
        String code = ShortCodeGenerator.generateUniqueShortCode(testUserId);
        assertTrue(ShortCodeGenerator.getUserUsedCodes(testUserId).contains(code));

        ShortCodeGenerator.releaseCodeForUser(testUserId, code);
        assertFalse(ShortCodeGenerator.getUserUsedCodes(testUserId).contains(code));
    }
}
