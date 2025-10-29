package com.example.urlshortener.core;

public class EmptyUrlException extends RuntimeException {
    public EmptyUrlException(String message) {
        super(message);
    }
}
