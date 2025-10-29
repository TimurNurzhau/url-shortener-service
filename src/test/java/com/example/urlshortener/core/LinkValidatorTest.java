package com.example.urlshortener.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

class LinkValidatorTest {
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
    }

    @Test
    void testIsLinkActive_ActiveLink() {
        Link link =
                new Link(
                        "https://example.com",
                        "test1234",
                        testUserId,
                        10,
                        Instant.now().plusSeconds(3600) // 1 час в будущем
                        );

        assertTrue(LinkValidator.isLinkActive(link));
    }

    @Test
    void testIsLinkActive_ExpiredLink() {
        Link link =
                new Link(
                        "https://example.com",
                        "test1234",
                        testUserId,
                        10,
                        Instant.now().minusSeconds(3600) // 1 час в прошлом
                        );

        assertFalse(LinkValidator.isLinkActive(link));
    }

    @Test
    void testIsLinkActive_ExceededClickLimit() {
        Link link =
                new Link(
                        "https://example.com",
                        "test1234",
                        testUserId,
                        2, // Лимит 2 клика
                        Instant.now().plusSeconds(3600));

        // Имитируем 3 клика
        link.incrementClicks();
        link.incrementClicks();
        link.incrementClicks();

        assertFalse(LinkValidator.isLinkActive(link));
    }

    @Test
    void testIsLinkActive_NullLink() {
        assertFalse(LinkValidator.isLinkActive(null));
    }

    @Test
    void testIsLinkActive_AtClickLimit() {
        Link link =
                new Link(
                        "https://example.com",
                        "test1234",
                        testUserId,
                        3,
                        Instant.now().plusSeconds(3600));

        link.incrementClicks();
        link.incrementClicks();
        link.incrementClicks(); // Ровно на лимите

        assertFalse(LinkValidator.isLinkActive(link));
    }
}
