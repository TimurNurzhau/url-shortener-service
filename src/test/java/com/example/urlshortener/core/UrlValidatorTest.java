package com.example.urlshortener.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UrlValidatorTest {

    @Test
    void testValidateUrl_ValidUrls() {
        assertDoesNotThrow(
                () -> {
                    UrlValidator.validateUrl("https://www.example.com");
                    UrlValidator.validateUrl("http://example.com");
                    UrlValidator.validateUrl("https://sub.domain.co.uk/path?query=param");
                });
    }

    @Test
    void testValidateUrl_EmptyUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    UrlValidator.validateUrl("");
                });

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    UrlValidator.validateUrl("   ");
                });
    }

    @Test
    void testNormalizeUrl() {
        assertEquals("https://example.com", UrlValidator.normalizeUrl("example.com"));
        assertEquals("http://example.com", UrlValidator.normalizeUrl("http://example.com"));
        assertEquals("https://example.com", UrlValidator.normalizeUrl("https://example.com"));
    }

    @Test
    void testIsValidUrl() {
        assertTrue(UrlValidator.isValidUrl("https://www.example.com"));
        assertTrue(UrlValidator.isValidUrl("http://example.com:8080"));
        assertFalse(UrlValidator.isValidUrl("not-a-url"));
        assertFalse(UrlValidator.isValidUrl(""));
        assertFalse(UrlValidator.isValidUrl(null));
    }
}
