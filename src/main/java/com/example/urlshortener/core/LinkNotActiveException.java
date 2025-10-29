package com.example.urlshortener.core;

public class LinkNotActiveException extends RuntimeException {
    public LinkNotActiveException(String message) {
        super(message);
    }
}
