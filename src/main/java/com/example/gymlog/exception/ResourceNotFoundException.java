package com.example.gymlog.exception;

/**
 * Thrown when a page asks for a workout or exercise that does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}